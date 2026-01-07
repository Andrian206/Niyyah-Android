package com.pab.niyyah.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pab.niyyah.R
import com.pab.niyyah.databinding.FragmentProfileBinding
import com.pab.niyyah.ui.auth.AuthActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        val username = document.getString("username") ?: ""

                        binding.tvName.text = "$firstName $lastName".trim().ifEmpty { "Name" }
                        binding.tvUsername.text = if (username.isNotEmpty()) "@$username" else ""
                    }
                }
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Edit button
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        // Logout button
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(context, "Berhasil logout!", Toast.LENGTH_SHORT).show()

            // Pindah ke AuthActivity
            val intent = Intent(requireActivity(), AuthActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
