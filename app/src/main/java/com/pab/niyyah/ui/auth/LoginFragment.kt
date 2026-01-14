package com.pab.niyyah.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.pab.niyyah.R
import com.pab.niyyah.databinding.FragmentLoginBinding
import com.pab.niyyah.ui.main.MainActivity

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setupActions()
    }

    private fun setupActions() {
        // 1. Link ke Register (Sign Up)
        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // 2. Link Lupa Password
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                binding.etEmail.error = "Masukkan email dulu"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }
            sendResetPassword(email)
        }

        // 3. Toggle Visibility Password (Mata)
        binding.ivPasswordToggle.setOnClickListener {
            togglePasswordVisibility(binding.etPassword)
        }

        // 4. Tombol Sign In (Login Email/Pass)
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginWithFirebase(email, password)
            }
        }

        // 5. Tombol Google Sign In
        binding.btnGoogleSignIn.setOnClickListener {
            Toast.makeText(context, "Fitur Google Login perlu setup SHA-1 di Firebase Console", Toast.LENGTH_LONG).show()
            // Nanti bisa kita tambahkan logika GoogleSignInClient di sini
        }
    }

    private fun validateInput(email: String, pass: String): Boolean {
        if (email.isEmpty()) {
            binding.etEmail.error = "Email wajib diisi"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Format email salah"
            return false
        }
        if (pass.isEmpty()) {
            binding.etPassword.error = "Password wajib diisi"
            return false
        }
        return true
    }

    private fun loginWithFirebase(email: String, pass: String) {
        // Tampilkan loading jika ada progress bar (opsional)
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navigateToHome()
                } else {
                    Toast.makeText(context, "Login Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendResetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(context, "Cek email Anda untuk reset password", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Gagal: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun togglePasswordVisibility(editText: EditText) {
        if (isPasswordVisible) {
            // Sembunyikan Password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            // Ubah icon mata dicoret (sesuaikan drawable anda)
            binding.ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
        } else {
            // Tampilkan Password
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            // Ubah icon mata terbuka
            binding.ivPasswordToggle.setImageResource(R.drawable.ic_visibility) // Ganti icon yg sesuai
        }
        // Pindahkan kursor ke akhir teks
        editText.setSelection(editText.text.length)
        isPasswordVisible = !isPasswordVisible
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