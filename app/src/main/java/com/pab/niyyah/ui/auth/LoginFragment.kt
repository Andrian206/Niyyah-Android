package com.pab.niyyah.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.pab.niyyah.R
import com.pab.niyyah.databinding.FragmentLoginBinding
import com.pab.niyyah.ui.main.MainActivity

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Client untuk Google Login
    private lateinit var googleSignInClient: GoogleSignInClient

    private var isPasswordVisible = false

    // 1. Launcher: Menangkap Hasil Login Google
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            // Ambil akun Google yang berhasil login
            val account = task.getResult(ApiException::class.java)
            if (account != null) {
                // Tukar Token Google ke Firebase Auth
                firebaseAuthWithGoogle(account.idToken!!)
            }
        } catch (e: ApiException) {
            Log.e("GoogleLogin", "Google Sign In Gagal code: ${e.statusCode}")
            Toast.makeText(context, "Gagal Login Google: ${e.statusCode}", Toast.LENGTH_SHORT).show()
        }
    }

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
        db = FirebaseFirestore.getInstance()

        setupGoogleClient()
        setupActions()
    }

    private fun setupGoogleClient() {
        // R.string.default_web_client_id dibuat OTOMATIS oleh google-services.json
        // Jika merah, coba Build > Rebuild Project. Jangan dibuat manual di strings.xml
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupActions() {
        // 1. Link ke Register
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

        // 3. Toggle Visibility Password
        binding.ivPasswordToggle.setOnClickListener {
            togglePasswordVisibility(binding.etPassword)
        }

        // 4. Tombol Sign In (Email/Pass)
        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginWithFirebase(email, password)
            }
        }

        // 5. Tombol Google Sign In
        binding.btnGoogleSignIn.setOnClickListener {
            signInGoogle()
        }
    }

    // --- LOGIKA GOOGLE SIGN IN ---

    private fun signInGoogle() {
        val intent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(intent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // Cek apakah user baru pertama kali login?
                    val isNewUser = task.result.additionalUserInfo?.isNewUser == true

                    if (isNewUser && user != null) {
                        // Jika user baru, simpan data profil dari Google ke Firestore
                        saveGoogleUserToFirestore(user)
                    } else {
                        // Jika user lama, langsung masuk Home
                        navigateToHome()
                    }
                } else {
                    Toast.makeText(context, "Autentikasi Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveGoogleUserToFirestore(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        // Ambil nama dari Google
        val fullName = firebaseUser.displayName ?: "User Google"
        val parts = fullName.split(" ")
        val firstName = parts.firstOrNull() ?: ""
        val lastName = parts.drop(1).joinToString(" ")

        // Ambil Foto URL
        val photoUrl = firebaseUser.photoUrl?.toString() ?: ""

        val userData = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to (firebaseUser.email ?: ""),
            "photoUrl" to photoUrl,
            "username" to "",
            "gender" to "",
            "nationality" to "",
            "birthDate" to "",
            "phoneNumber" to ""
        )

        db.collection("users").document(firebaseUser.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener { navigateToHome() }
            .addOnFailureListener { navigateToHome() } // Tetap masuk meski gagal save profil
    }

    // --- LOGIKA EMAIL/PASSWORD & UTIL ---

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
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivPasswordToggle.setImageResource(R.drawable.ic_visibility_off)
        } else {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivPasswordToggle.setImageResource(R.drawable.ic_visibility)
        }
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