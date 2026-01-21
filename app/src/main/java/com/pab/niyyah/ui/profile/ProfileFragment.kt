package com.pab.niyyah.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pab.niyyah.R
import com.pab.niyyah.databinding.FragmentProfileBinding
import com.pab.niyyah.ui.auth.AuthActivity
import com.pab.niyyah.utils.ImageUtils

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
        val currentUser = auth.currentUser ?: return

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (_binding == null) return@addOnSuccessListener

                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val username = document.getString("username") ?: ""

                    val fullName = "$firstName $lastName".trim()
                    binding.tvName.text = fullName.ifEmpty { "Name" }
                    binding.tvUsername.text = if (username.isNotEmpty()) "@$username" else ""

                    // --- LOAD GAMBAR BASE64 dengan ImageUtils ---
                    val photoString = document.getString("photoUrl")
                    ImageUtils.loadBase64Image(binding.ivAvatar, photoString)
                }
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener
                Toast.makeText(context, "Gagal memuat profil: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { findNavController().navigateUp() }
        binding.btnEdit.setOnClickListener { findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment) }
        binding.btnLogout.setOnClickListener { showLogoutConfirmation() }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ -> performLogout() }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        val intent = Intent(requireActivity(), AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}