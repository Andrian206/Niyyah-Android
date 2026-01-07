package com.pab.niyyah.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pab.niyyah.R
import com.pab.niyyah.databinding.FragmentHomeBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

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

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {

        // Set greeting dengan nama user
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: ""
                        binding.tvGreeting.text = getString(R.string.hello_user, firstName)
                    }
                }
        }

        // Setup RecyclerView
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        // TODO: Setup adapter untuk tasks

        // Set progress (contoh: 0%)
        binding.tvProgressPercent.text = getString(R.string.progress_percent, 0)
    }

    private fun setupClickListeners() {
        // FAB Add Task
        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_createTaskFragment)
        }

        // Avatar -> Profile
        binding.ivAvatar.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
