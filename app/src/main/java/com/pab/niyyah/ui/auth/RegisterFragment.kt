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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.pab.niyyah.R
import com.pab.niyyah.databinding.FragmentRegisterBinding
import com.pab.niyyah.ui.main.MainActivity

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupPasswordToggles()
        setupClickListeners()
    }
    
    private fun setupPasswordToggles() {
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
        
        binding.ivConfirmPasswordToggle.setOnClickListener {
            isConfirmPasswordVisible = !isConfirmPasswordVisible
            if (isConfirmPasswordVisible) {
                binding.etConfirmPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.ivConfirmPasswordToggle.setImageResource(R.drawable.ic_visibility)
            } else {
                binding.etConfirmPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.ivConfirmPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
            }
            binding.etConfirmPassword.setSelection(binding.etConfirmPassword.text?.length ?: 0)
        }
    }
    
    private fun setupClickListeners() {
        binding.tvSignIn.setOnClickListener {
            if (!isLoading) {
                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
            }
        }

        binding.btnSignUp.setOnClickListener {
            if (isLoading) return@setOnClickListener
            
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (!validateInput(firstName, lastName, email, password, confirmPassword)) {
                return@setOnClickListener
            }

            registerUser(firstName, lastName, email, password)
        }
    }
    
    private fun validateInput(
        firstName: String, 
        lastName: String, 
        email: String, 
        password: String, 
        confirmPassword: String
    ): Boolean {
        if (firstName.isEmpty()) {
            binding.etFirstName.error = "Nama depan harus diisi"
            binding.etFirstName.requestFocus()
            return false
        }
        
        if (lastName.isEmpty()) {
            binding.etLastName.error = "Nama belakang harus diisi"
            binding.etLastName.requestFocus()
            return false
        }
        
        if (email.isEmpty()) {
            binding.etEmail.error = "Email harus diisi"
            binding.etEmail.requestFocus()
            return false
        }
        
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Format email tidak valid"
            binding.etEmail.requestFocus()
            return false
        }
        
        if (password.isEmpty()) {
            binding.etPassword.error = "Password harus diisi"
            binding.etPassword.requestFocus()
            return false
        }
        
        if (password.length < 6) {
            binding.etPassword.error = "Password minimal 6 karakter"
            binding.etPassword.requestFocus()
            return false
        }
        
        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.error = "Konfirmasi password harus diisi"
            binding.etConfirmPassword.requestFocus()
            return false
        }
        
        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Password tidak cocok"
            binding.etConfirmPassword.requestFocus()
            return false
        }
        
        return true
    }
    
    private fun setLoading(loading: Boolean) {
        isLoading = loading
        binding.btnSignUp.isEnabled = !loading
        binding.btnSignUp.text = if (loading) "Mendaftar..." else "Sign Up"
        binding.progressBar?.isVisible = loading
    }

    private fun registerUser(firstName: String, lastName: String, email: String, pass: String) {
        setLoading(true)
        
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    saveUserToFirestore(userId, firstName, lastName, email)
                } else {
                    setLoading(false)
                    showError("Gagal mendapatkan ID pengguna")
                }
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                showError(getFirebaseErrorMessage(exception))
            }
    }

    private fun saveUserToFirestore(userId: String, firstName: String, lastName: String, email: String) {
        val userData = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "username" to "",
            "photoUrl" to "",
            "gender" to "",
            "nationality" to "",
            "birthDate" to "",
            "phoneNumber" to ""
        )

        db.collection("users").document(userId).set(userData)
            .addOnSuccessListener {
                setLoading(false)
                Toast.makeText(context, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()

                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .addOnFailureListener { exception ->
                setLoading(false)
                showError("Gagal menyimpan data: ${exception.localizedMessage}")
            }
    }
    
    private fun getFirebaseErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthUserCollisionException -> "Email sudah terdaftar"
            is FirebaseAuthWeakPasswordException -> "Password terlalu lemah"
            is FirebaseAuthEmailException -> "Format email tidak valid"
            is FirebaseNetworkException -> "Tidak ada koneksi internet"
            else -> exception.localizedMessage ?: "Registrasi gagal"
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}