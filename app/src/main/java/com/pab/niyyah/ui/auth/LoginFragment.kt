package com.pab.niyyah.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    private var isLoading = false

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

        setupPasswordToggle()
        setupClickListeners()
    }
    
    private fun setupPasswordToggle() {
        binding.ivPasswordToggle.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.ivPasswordToggle.setImageResource(R.drawable.ic_visibility)
            } else {
                binding.etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
            }
            binding.etPassword.setSelection(binding.etPassword.text?.length ?: 0)
        }
    }
    
    private fun setupClickListeners() {
        binding.tvSignUp.setOnClickListener {
            if (!isLoading) {
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
        }

        binding.btnSignIn.setOnClickListener {
            if (!isLoading) {
                performLogin()
            }
        }
    }
    
    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validasi input
        if (!validateInput(email, password)) return

        // Set loading state
        setLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (_binding == null) return@addOnCompleteListener
                
                setLoading(false)
                
                if (task.isSuccessful) {
                    Toast.makeText(context, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                } else {
                    val errorMessage = getFirebaseErrorMessage(task.exception)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(context, "Email wajib diisi!", Toast.LENGTH_SHORT).show()
            binding.etEmail.requestFocus()
            return false
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Format email tidak valid!", Toast.LENGTH_SHORT).show()
            binding.etEmail.requestFocus()
            return false
        }
        
        if (password.isEmpty()) {
            Toast.makeText(context, "Password wajib diisi!", Toast.LENGTH_SHORT).show()
            binding.etPassword.requestFocus()
            return false
        }
        
        if (password.length < 6) {
            Toast.makeText(context, "Password minimal 6 karakter!", Toast.LENGTH_SHORT).show()
            binding.etPassword.requestFocus()
            return false
        }
        
        return true
    }
    
    private fun setLoading(loading: Boolean) {
        isLoading = loading
        binding.btnSignIn.isEnabled = !loading
        binding.btnSignIn.text = if (loading) "Loading..." else getString(R.string.sign_in)
    }
    
    private fun getFirebaseErrorMessage(exception: Exception?): String {
        return when {
            exception?.message?.contains("no user record") == true -> 
                "Email tidak terdaftar"
            exception?.message?.contains("password is invalid") == true -> 
                "Password salah"
            exception?.message?.contains("network") == true -> 
                "Tidak ada koneksi internet"
            else -> 
                "Login gagal: ${exception?.message}"
        }
    }
    
    private fun navigateToMain() {
        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}