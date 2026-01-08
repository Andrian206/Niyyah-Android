package com.pab.niyyah.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.pab.niyyah.R
import com.pab.niyyah.ui.auth.AuthActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()
        println("FIREBASE CHECK: Aplikasi berhasil terhubung! User saat ini: ${auth.currentUser}")
        var isReady = false
        Handler(Looper.getMainLooper()).postDelayed({
            val currentUser = auth.currentUser

            if (currentUser == null) {
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)

                finish()
            } else {
                 isReady = true
            }
           
        }, 1000)

        splashScreen.setKeepOnScreenCondition { !isReady }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}