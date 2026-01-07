package com.pab.niyyah.ui.auth

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
import com.pab.niyyah.databinding.FragmentRegisterBinding
import com.pab.niyyah.ui.main.MainActivity

class RegisterFragment : Fragment() {

    // Setup ViewBinding
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    // Setup Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 1. Aksi Tombol Login (Pindah ke Login jika sudah punya akun)
        binding.tvSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        // 2. Aksi Tombol Register
        binding.btnSignUp.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            // Validasi Input Kosong
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Semua kolom harus diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi Password Match
            if (password != confirmPassword) {
                Toast.makeText(context, "Password tidak cocok!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mulai Proses Register Firebase
            registerUser(firstName, lastName, email, password)
        }
    }

    private fun registerUser(firstName: String, lastName: String, email: String, pass: String) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sukses Login Auth, sekarang simpan data ke Firestore
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserToFirestore(userId, firstName, lastName, email)
                    }
                } else {
                    Toast.makeText(context, "Register Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserToFirestore(userId: String, firstName: String, lastName: String, email: String) {
        // Buat map data user
        val userData = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "username" to "",
            "photoUrl" to "",
            "gender" to "",
            "nationality" to ""
        )

        db.collection("users").document(userId).set(userData)
            .addOnSuccessListener {
                Toast.makeText(context, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()

                // Pindah ke MainActivity (Home)
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal simpan data user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}