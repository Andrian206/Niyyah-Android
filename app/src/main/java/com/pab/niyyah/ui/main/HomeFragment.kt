package com.pab.niyyah.ui.main

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.pab.niyyah.R
import com.pab.niyyah.data.Task
import com.pab.niyyah.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var todayAdapter: TaskAdapter
    private lateinit var futureAdapter: TaskAdapter
    private lateinit var completedAdapter: TaskAdapter

    private var tasksListener: ListenerRegistration? = null

    // Default State: Today terbuka, sisanya tertutup
    private var isTodayExpanded = true
    private var isFutureExpanded = false
    private var isCompletedExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) return

        setupUI(currentUser.uid)
        setupClickListeners()
        setupSectionHeaders()
        loadTasksFromFirebase(currentUser.uid)
    }

    private fun setupUI(userId: String) {
        // Ambil nama user untuk header
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (_binding != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    binding.tvGreeting.text = "Hello, $firstName!"
                }
            }

        // Listener umum untuk klik task dan checkbox
        val onTaskClicked = { task: Task ->
            val bundle = bundleOf("taskId" to task.id)
            findNavController().navigate(R.id.action_homeFragment_to_editTaskFragment, bundle)
        }

        val onCheckboxClicked = { task: Task ->
            toggleTaskCompleted(task)
        }

        // Inisialisasi Adapter
        todayAdapter = TaskAdapter(onTaskClicked, onCheckboxClicked)
        futureAdapter = TaskAdapter(onTaskClicked, onCheckboxClicked)
        completedAdapter = TaskAdapter(onTaskClicked, onCheckboxClicked)

        binding.rvTodayTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = todayAdapter
        }
        binding.rvFutureTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = futureAdapter
        }
        binding.rvCompletedTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = completedAdapter
        }

        // Set visibilitas awal sesuai state
        refreshSectionVisibility(animate = false)
    }

    private fun setupSectionHeaders() {
        binding.layoutTodayHeader.setOnClickListener {
            isTodayExpanded = !isTodayExpanded
            refreshSectionVisibility(animate = true)
        }
        binding.layoutFutureHeader.setOnClickListener {
            isFutureExpanded = !isFutureExpanded
            refreshSectionVisibility(animate = true)
        }
        binding.layoutCompletedHeader.setOnClickListener {
            isCompletedExpanded = !isCompletedExpanded
            refreshSectionVisibility(animate = true)
        }
    }

    private fun refreshSectionVisibility(animate: Boolean) {
        if (_binding == null) return

        if (animate) {
            // Efek animasi slide halus
            TransitionManager.beginDelayedTransition(binding.cardTask, AutoTransition())
        }

        // Update Today
        binding.rvTodayTasks.isVisible = isTodayExpanded
        binding.ivTodayDropdown.rotation = if (isTodayExpanded) 0f else 180f

        // Update Future
        binding.rvFutureTasks.isVisible = isFutureExpanded
        binding.ivFutureDropdown.rotation = if (isFutureExpanded) 0f else 180f

        // Update Completed
        binding.rvCompletedTasks.isVisible = isCompletedExpanded
        binding.ivCompletedDropdown.rotation = if (isCompletedExpanded) 0f else 180f
    }

    private fun setupClickListeners() {
        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createTaskFragment)
        }
        binding.ivAvatar.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    private fun loadTasksFromFirebase(userId: String) {
        tasksListener = db.collection("tasks")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, error ->
                if (_binding == null) return@addSnapshotListener

                if (error != null) {
                    Toast.makeText(context, "Gagal sinkronisasi data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val allTasks = snapshots?.documents?.mapNotNull { document ->
                    document.toObject(Task::class.java)?.copy(id = document.id)
                } ?: emptyList()

                // Format tanggal hari ini (Pastikan Locale.US agar cocok dengan database)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
                val todayDateStr = dateFormat.format(Date())

                // --- FILTERING LOGIC (PERBAIKAN DISINI) ---

                // 1. TODAY: Hanya yang BELUM SELESAI (!isCompleted) DAN Tanggalnya HARI INI
                val todayTasks = allTasks.filter { task ->
                    !task.isCompleted && task.dueDate == todayDateStr
                }.sortedBy { it.time }

                // 2. FUTURE: Hanya yang BELUM SELESAI (!isCompleted) DAN (Tanggalnya nanti ATAU kosong) DAN (Bukan hari ini)
                val futureTasks = allTasks.filter { task ->
                    !task.isCompleted && (task.dueDate.isEmpty() || isDateAfterToday(task.dueDate)) && task.dueDate != todayDateStr
                }.sortedBy { it.dueDate }

                // 3. COMPLETED: Hanya yang SUDAH SELESAI (isCompleted == true)
                // Logic ini akan menangkap task "h" yang baru saja dicentang
                val completedTasks = allTasks.filter { task ->
                    task.isCompleted
                }.sortedByDescending { it.createdAt } // Urutkan dari yang baru selesai

                // --- UPDATE ADAPTER ---
                // Submit data baru ke List UI
                todayAdapter.submitList(todayTasks)
                futureAdapter.submitList(futureTasks)
                completedAdapter.submitList(completedTasks)

                // Update Progress Bar %
                updateProgress(allTasks)

                // Opsional: Jika ada task selesai baru, otomatis buka dropdown completed biar user sadar tasknya pindah
                if (completedTasks.isNotEmpty() && !isCompletedExpanded) {
                    isCompletedExpanded = true
                    refreshSectionVisibility(true)
                }
            }
    }

    // --- Helper Logic Tanggal ---

    // PENTING: Gunakan Locale.US agar format tanggal konsisten di semua HP (tidak berubah jadi huruf Arab/Cina dll)
    private fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        return dateFormat.format(Date())
    }

    private fun isDateAfterToday(dateStr: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val taskDate = dateFormat.parse(dateStr)
            val today = Date()

            // Logika sederhana membandingkan tanggal
            taskDate != null && taskDate.after(today)
        } catch (e: Exception) {
            false // Jika format salah, anggap bukan future
        }
    }

    private fun toggleTaskCompleted(task: Task) {
        val newStatus = !task.isCompleted

        // Optimis UI update: update dulu di adapter sebelum firebase selesai (optional)
        // Tapi Firestore listener cukup cepat, jadi kita andalkan listener saja.

        db.collection("tasks").document(task.id)
            .update("isCompleted", newStatus)
            .addOnSuccessListener {
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal update status", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProgress(allTasks: List<Task>) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val todayDateStr = dateFormat.format(Date())

        val todayTasks = allTasks.filter { task ->
            task.dueDate == todayDateStr
        }

        // 3. Cek jika tidak ada tugas hari ini, set 0%
        if (todayTasks.isEmpty()) {
            binding.progressBar.progress = 0
            binding.tvProgressPercent.text = "0%"
            return
        }

        // 4. Hitung yang selesai HANYA dari list 'todayTasks'
        val completed = todayTasks.count { it.isCompleted }
        val totalToday = todayTasks.size

        // 5. Rumus Persentase
        val percentage = (completed.toFloat() / totalToday * 100).toInt()

        // 6. Update UI
        binding.progressBar.setProgress(percentage, true)
        binding.tvProgressPercent.text = "$percentage%"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tasksListener?.remove()
        _binding = null
    }
}