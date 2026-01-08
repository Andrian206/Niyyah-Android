package com.pab.niyyah.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.pab.niyyah.R
import com.pab.niyyah.ui.auth.AuthActivity

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var isCheckingAuth = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen sebelum super.onCreate()
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // Keep splash screen sampai auth check selesai
        splashScreen.setKeepOnScreenCondition { isCheckingAuth }

        // Check authentication status
        checkAuthAndNavigate()

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun checkAuthAndNavigate() {
        val currentUser = auth.currentUser
        
        if (currentUser == null) {
            // User belum login, pindah ke AuthActivity
            navigateToAuth()
        } else {
            // User sudah login, tampilkan HomeFragment
            isCheckingAuth = false
        }
    }

    private fun navigateToAuth() {
        val intent = Intent(this, AuthActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}