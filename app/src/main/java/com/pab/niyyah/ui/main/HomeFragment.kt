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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var ongoingTaskAdapter: TaskAdapter
    private lateinit var completedTaskAdapter: TaskAdapter
    
    private var tasksListener: ListenerRegistration? = null

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

        ongoingTaskAdapter = TaskAdapter(
            onTaskClick = { task ->
                val bundle = bundleOf("taskId" to task.id)
                findNavController().navigate(R.id.action_homeFragment_to_editTaskFragment, bundle)
            },
            onCheckboxClick = { task ->
                toggleTaskCompleted(task)
            }
        )

        completedTaskAdapter = TaskAdapter(
            onTaskClick = { task ->
                val bundle = bundleOf("taskId" to task.id)
                findNavController().navigate(R.id.action_homeFragment_to_editTaskFragment, bundle)
            },
            onCheckboxClick = { task ->
                toggleTaskCompleted(task)
            }
        )

        binding.rvOngoingTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ongoingTaskAdapter
        }

        binding.rvCompletedTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = completedTaskAdapter
        }

        binding.tvProgressPercent.text = getString(R.string.progress_percent, 0)
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

                val ongoingTasks = allTasks.filter { !it.isCompleted }.sortedByDescending { it.createdAt }
                val completedTasks = allTasks.filter { it.isCompleted }.sortedByDescending { it.createdAt }

                ongoingTaskAdapter.submitList(ongoingTasks)
                completedTaskAdapter.submitList(completedTasks)

                binding.tvNoOngoingTask.isVisible = ongoingTasks.isEmpty()
                binding.rvOngoingTasks.isVisible = ongoingTasks.isNotEmpty()

                binding.tvNoCompletedTask.isVisible = completedTasks.isEmpty()
                binding.rvCompletedTasks.isVisible = completedTasks.isNotEmpty()

                updateProgress(allTasks)
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
