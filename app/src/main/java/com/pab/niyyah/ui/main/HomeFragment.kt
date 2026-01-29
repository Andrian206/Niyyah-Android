package com.pab.niyyah.ui.main

import android.graphics.BitmapFactory
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.pab.niyyah.R
import com.pab.niyyah.data.Task
import com.pab.niyyah.data.TaskUiModel
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

    // Adapter Tunggal
    private lateinit var mainAdapter: TaskAdapter
    
    // Set untuk menyimpan judul section yang DITUTUP (Collapsed)
    private val collapsedSections = mutableSetOf<String>("Completed")

    private var tasksListener: ListenerRegistration? = null
    
    // Cache data terakhir untuk keperluan toggle tanpa fetch ulang
    private var lastRawTasks: List<Task> = emptyList()

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

        val userId = auth.currentUser?.uid ?: return

        setupUI(userId)
        loadTasksFromFirebase(userId)
    }

    private fun setupUI(userId: String) {
        // 1. Setup Header User & Foto Profil
        db.collection("users").document(userId).get().addOnSuccessListener { document ->
            if (_binding != null && document.exists()) {
                val firstName = document.getString("firstName") ?: "User"
                binding.tvGreeting.text = "Hello, $firstName!"

                // Decode Base64 Foto
                val photoString = document.getString("photoUrl")
                if (!photoString.isNullOrEmpty() && !photoString.startsWith("http")) {
                    try {
                        val decodedBytes = Base64.decode(photoString, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        binding.ivAvatar.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        binding.ivAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
                    }
                }
            }
        }

        // 2. Setup Tombol & Navigasi
        binding.fabAddTask.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_createTaskFragment) }
        binding.ivAvatar.setOnClickListener { findNavController().navigate(R.id.action_homeFragment_to_profileFragment) }

        // 3. Setup Adapters
        // Callback saat item diklik
        val onTaskClick = { task: Task ->
            val bundle = bundleOf("taskId" to task.id)
            findNavController().navigate(R.id.action_homeFragment_to_editTaskFragment, bundle)
        }
        val onCheckClick = { task: Task -> toggleTaskStatus(task) }
        
        // Callback Header
        val onHeaderClick = { title: String -> 
            toggleSection(title) 
        }

        mainAdapter = TaskAdapter(onTaskClick, onCheckClick, onHeaderClick)

        // Setup RecyclerView Tunggal
        binding.rvActiveTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = mainAdapter
            itemAnimator = null // Matikan animasi kedip
        }
    }

    private fun loadTasksFromFirebase(userId: String) {
        tasksListener = db.collection("tasks")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshots, e ->
                if (_binding == null || e != null) return@addSnapshotListener

                val allTasks = snapshots?.documents?.mapNotNull {
                    it.toObject(Task::class.java)?.copy(id = it.id)
                } ?: emptyList()

                lastRawTasks = allTasks

                // Panggil fungsi pengelompokan data
                processTasksToUi(allTasks)
            }
    }

    // --- LOGIKA UTAMA: PENGELOMPOKAN DATA ---
    private fun toggleSection(title: String) {
        if (collapsedSections.contains(title)) {
            collapsedSections.remove(title) // Jadi Expanded
        } else {
            collapsedSections.add(title) // Jadi Collapsed
        }
        // Re-process UI dengan state baru
        processTasksToUi(lastRawTasks)
    }

    // --- LOGIKA UTAMA: PENGELOMPOKAN DATA ---
    private fun processTasksToUi(allTasks: List<Task>) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        
        val todayDate = Date()
        val todayStr = dateFormat.format(todayDate) 
        
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrowStr = dateFormat.format(cal.time)

        // 1. Pisahkan: Selesai vs Belum Selesai (Active)
        val (completed, active) = allTasks.partition { it.isCompleted }

        // List untuk menampung hasil susunan (Header + Item)
        val groupedList = mutableListOf<TaskUiModel>()

        // Helper untuk tambah Section agar rapi
        fun addSection(title: String, tasks: List<Task>) {
            if (tasks.isEmpty()) return

            // Cek apakah judul ini ada di daftar "Tertutup"?
            val isExpanded = !collapsedSections.contains(title)

            // Tambah Header (Kirim status isExpanded ke Adapter)
            groupedList.add(TaskUiModel.Header(title, isExpanded, tasks.size))

            // Cuma tambah Item kalau Expanded
            if (isExpanded) {
                tasks.forEach { groupedList.add(TaskUiModel.Item(it)) }
            }
        }

        // A. Overdue
        val overdueTasks = active.filter {
            try {
               isDateBeforeToday(it.dueDate, todayStr)
            } catch (e: Exception) { false }
        }.sortedBy { it.dueDate }
        
        addSection("Overdue", overdueTasks)

        // B. Today
        val todayTasks = active.filter { it.dueDate == todayStr }.sortedBy { it.time }
        addSection("Today", todayTasks)

        // C. Tomorrow
        val tomorrowTasks = active.filter { it.dueDate == tomorrowStr }.sortedBy { it.time }
        addSection("Tomorrow", tomorrowTasks)

        // D. Future (Lainnya)
        val futureTasks = active.filter {
            it.dueDate != todayStr && it.dueDate != tomorrowStr && !overdueTasks.contains(it)
        }.sortedBy { it.dueDate }

        // Kelompokkan Future per Tanggal
        val futureGrouped = futureTasks.groupBy { it.dueDate }
        futureGrouped.forEach { (date, tasks) ->
             addSection(date, tasks)
        }
        
        // E. Completed
        val completedSorted = completed.sortedByDescending { it.createdAt }
        addSection("Completed", completedSorted)

        // 2. Submit Data ke Adapter
        mainAdapter.submitList(groupedList)

        // 3. Update Progress Bar
        updateProgress(todayTasks, completed.filter { it.dueDate == todayStr })
    }
    
    // Helper kecil untuk tanggal
    private fun isDateBeforeToday(date: String, todayStr: String): Boolean {
        if (date.isEmpty() || date == todayStr) return false
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        return try {
             val d1 = sdf.parse(date)
             val d2 = sdf.parse(todayStr)
             d1 != null && d1.before(d2)
        } catch (e: Exception) { false }
    }

    private fun updateProgress(activeToday: List<Task>, completedToday: List<Task>) {
        val total = activeToday.size + completedToday.size
        if (total == 0) {
            binding.tvProgressPercent.text = "0%"
            binding.progressBar.progress = 0
            return
        }
        val percentage = (completedToday.size.toFloat() / total * 100).toInt()
        binding.tvProgressPercent.text = "$percentage%"
        binding.progressBar.setProgress(percentage, true)
    }

    private fun toggleTaskStatus(task: Task) {
        val newStatus = !task.isCompleted

        // 1. Update status tugas yang diklik (Ini wajib dilakukan apapun kondisinya)
        db.collection("tasks").document(task.id).update("isCompleted", newStatus)

        // 2. LOGIKA REPEAT: Hanya jalan jika tugas BARU SAJA diselesaikan (newStatus == true)
        // DAN punya jadwal ulang (repeat != "Never")
        if (newStatus && task.repeat != "Never") {
            createNewRepeatTask(task)
        }
    }

    private fun createNewRepeatTask(originalTask: Task) {
        // Hitung tanggal berikutnya
        val nextDate = getNextDueDate(originalTask.dueDate, originalTask.repeat)

        if (nextDate.isEmpty()) return // Jaga-jaga jika parsing tanggal gagal

        // Buat ID baru untuk tugas baru
        val newTaskId = db.collection("tasks").document().id

        // Kloning data tugas lama, tapi ganti ID, Tanggal, dan Status
        val newTask = originalTask.copy(
            id = newTaskId,
            dueDate = nextDate,       // Tanggal baru
            isCompleted = false,      // Reset jadi belum selesai
            createdAt = System.currentTimeMillis() // Waktu buat baru
        )

        // Simpan ke Firestore (Otomatis muncul di Future list nanti)
        db.collection("tasks").document(newTaskId).set(newTask)
            .addOnSuccessListener {
                Toast.makeText(context, "Tugas berikutnya dijadwalkan: $nextDate", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getNextDueDate(currentDateStr: String, repeatOption: String): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val calendar = Calendar.getInstance()

        try {
            val date = sdf.parse(currentDateStr) ?: return ""
            calendar.time = date

            when (repeatOption) {
                "Daily" -> calendar.add(Calendar.DAY_OF_YEAR, 1)   // Tambah 1 Hari
                "Weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1) // Tambah 1 Minggu
                "Monthly" -> calendar.add(Calendar.MONTH, 1)       // Tambah 1 Bulan
                else -> return "" // Never
            }

            return sdf.format(calendar.time)
        } catch (e: Exception) {
            return ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tasksListener?.remove()
        _binding = null
    }
}