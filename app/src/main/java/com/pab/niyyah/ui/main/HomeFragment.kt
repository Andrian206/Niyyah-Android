package com.pab.niyyah.ui.main

import android.os.Bundle
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
    
    // Section expand/collapse states
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

        val currentUser = auth.currentUser ?: return

        setupUI(currentUser.uid)
        setupClickListeners()
        setupSectionHeaders()
        loadTasksFromFirebase(currentUser.uid)
    }

    private fun setupUI(userId: String) {
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (_binding == null) return@addOnSuccessListener
                
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    binding.tvGreeting.text = getString(R.string.hello_user, firstName)
                }
            }

        // Setup Today Adapter
        todayAdapter = TaskAdapter(
            onTaskClick = { task ->
                val bundle = bundleOf("taskId" to task.id)
                findNavController().navigate(R.id.action_homeFragment_to_editTaskFragment, bundle)
            },
            onCheckboxClick = { task ->
                toggleTaskCompleted(task)
            }
        )
        
        // Setup Future Adapter
        futureAdapter = TaskAdapter(
            onTaskClick = { task ->
                val bundle = bundleOf("taskId" to task.id)
                findNavController().navigate(R.id.action_homeFragment_to_editTaskFragment, bundle)
            },
            onCheckboxClick = { task ->
                toggleTaskCompleted(task)
            }
        )
        
        // Setup Completed Adapter
        completedAdapter = TaskAdapter(
            onTaskClick = { task ->
                val bundle = bundleOf("taskId" to task.id)
                findNavController().navigate(R.id.action_homeFragment_to_editTaskFragment, bundle)
            },
            onCheckboxClick = { task ->
                toggleTaskCompleted(task)
            }
        )

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

        binding.tvProgressPercent.text = getString(R.string.progress_percent, 0)
        
        // Set initial visibility
        updateSectionVisibility()
    }
    
    private fun setupSectionHeaders() {
        binding.layoutTodayHeader.setOnClickListener {
            isTodayExpanded = !isTodayExpanded
            updateSectionVisibility()
        }
        
        binding.layoutFutureHeader.setOnClickListener {
            isFutureExpanded = !isFutureExpanded
            updateSectionVisibility()
        }
        
        binding.layoutCompletedHeader.setOnClickListener {
            isCompletedExpanded = !isCompletedExpanded
            updateSectionVisibility()
        }
    }
    
    private fun updateSectionVisibility() {
        // Today section
        binding.rvTodayTasks.isVisible = isTodayExpanded
        binding.ivTodayDropdown.setImageResource(
            if (isTodayExpanded) R.drawable.ic_dropdown_up else R.drawable.ic_dropdown_down
        )
        
        // Future section
        binding.rvFutureTasks.isVisible = isFutureExpanded
        binding.ivFutureDropdown.setImageResource(
            if (isFutureExpanded) R.drawable.ic_dropdown_up else R.drawable.ic_dropdown_down
        )
        
        // Completed section
        binding.rvCompletedTasks.isVisible = isCompletedExpanded
        binding.ivCompletedDropdown.setImageResource(
            if (isCompletedExpanded) R.drawable.ic_dropdown_up else R.drawable.ic_dropdown_down
        )
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
                    Toast.makeText(context, "Gagal ambil data: ${error.localizedMessage}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val allTasks = snapshots?.documents?.mapNotNull { document ->
                    document.toObject(Task::class.java)?.copy(id = document.id)
                } ?: emptyList()

                // Categorize tasks
                val todayDateStr = getTodayDateString()
                
                val todayTasks = allTasks.filter { task ->
                    !task.isCompleted && task.dueDate == todayDateStr
                }.sortedByDescending { it.createdAt }
                
                val futureTasks = allTasks.filter { task ->
                    !task.isCompleted && (task.dueDate.isEmpty() || isDateAfterToday(task.dueDate))
                }.sortedByDescending { it.createdAt }
                
                val completedTodayTasks = allTasks.filter { task ->
                    task.isCompleted
                }.sortedByDescending { it.createdAt }

                // Submit to adapters
                todayAdapter.submitList(null)
                todayAdapter.submitList(todayTasks)
                
                futureAdapter.submitList(null)
                futureAdapter.submitList(futureTasks)
                
                completedAdapter.submitList(null)
                completedAdapter.submitList(completedTodayTasks)

                updateProgress(allTasks)
            }
    }
    
    private fun getTodayDateString(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    private fun isDateAfterToday(dateStr: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = dateFormat.parse(dateStr)
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time
            date?.after(today) == true
        } catch (e: Exception) {
            false
        }
    }

    private fun toggleTaskCompleted(task: Task) {
        db.collection("tasks").document(task.id)
            .update("isCompleted", !task.isCompleted)
            .addOnSuccessListener {
                val status = if (!task.isCompleted) "selesai" else "belum selesai"
                Toast.makeText(context, "Task ditandai $status", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Gagal update: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProgress(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            binding.progressBar.progress = 0
            binding.tvProgressPercent.text = getString(R.string.progress_percent, 0)
            return
        }

        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val percentage = (completed.toFloat() / total.toFloat() * 100).toInt()

        binding.progressBar.progress = percentage
        binding.tvProgressPercent.text = getString(R.string.progress_percent, percentage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tasksListener?.remove()
        tasksListener = null
        _binding = null
    }
}
