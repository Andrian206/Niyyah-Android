package com.pab.niyyah.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.pab.niyyah.R
import com.pab.niyyah.data.User
import com.pab.niyyah.databinding.FragmentRegisterBinding
import com.pab.niyyah.ui.main.MainActivity

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var isPassVisible = false
    private var isConfirmPassVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupActions()
    }

    private fun setupActions() {
        // 1. Link kembali ke Login
        binding.tvSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        // 2. Toggle Password Utama
        binding.ivPasswordToggle.setOnClickListener {
            isPassVisible = togglePassword(binding.etPassword, binding.ivPasswordToggle, isPassVisible)
        }

        // 3. Toggle Password Konfirmasi
        binding.ivConfirmPasswordToggle.setOnClickListener {
            isConfirmPassVisible = togglePassword(binding.etConfirmPassword, binding.ivConfirmPasswordToggle, isConfirmPassVisible)
        }

        // 4. Tombol Register (Sign Up)
        binding.btnSignUp.setOnClickListener {
            handleRegister()
        }
    }

    // Fungsi pembantu untuk ubah input type password/text
    private fun togglePassword(editText: EditText, icon: ImageView, currentState: Boolean): Boolean {
        if (currentState) {
            // Sembunyikan
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            icon.setImageResource(R.drawable.ic_visibility)
        } else {
            // Tampilkan
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            icon.setImageResource(R.drawable.ic_visibility)
        }
        editText.setSelection(editText.text.length)
        return !currentState
    }

    private fun handleRegister() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPass = binding.etConfirmPassword.text.toString().trim()

        // --- VALIDASI ---
        if (firstName.isEmpty() || lastName.isEmpty()) {
            Toast.makeText(context, "Nama depan & belakang wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Email tidak valid"
            return
        }
        if (password.length < 8) {
            binding.etPassword.error = "Password minimal 8 karakter"
            return
        }
        if (password != confirmPass) {
            binding.etConfirmPassword.error = "Password tidak cocok"
            return
        }

        // --- PROSES CREATE USER FIREBASE ---
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        saveUserToFirestore(userId, firstName, lastName, email)
                    }
                } else {
                    Toast.makeText(context, "Register Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserToFirestore(uid: String, first: String, last: String, email: String) {
        // Menggunakan Data Class User yang baru Anda berikan
        val newUser = User(
            firstName = first,
            lastName = last,
            email = email,
            username = "", // Bisa diisi nanti di Edit Profile
            gender = "",
            nationality = "",
            birthDate = "",
            phoneNumber = "",
            photoUrl = ""
        )

        db.collection("users").document(uid).set(newUser)
            .addOnSuccessListener {
                Toast.makeText(context, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal simpan data: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHome() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}