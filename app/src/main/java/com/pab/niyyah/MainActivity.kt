package com.pab.niyyah

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Panggil installSplashScreen() SEBELUM super.onCreate()
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Variabel untuk menahan splash screen
        var isReady = false

        // 2. Tahan splash screen menggunakan setKeepOnScreenCondition
        // Kondisi: "Jangan hilangkan splash screen selama 'isReady' masih false"
        splashScreen.setKeepOnScreenCondition { !isReady }

        // 3. Simulasikan proses loading (misalnya 2 detik)
        // Gunakan Handler untuk memberi jeda sebelum menandai 'isReady = true'
        Handler(Looper.getMainLooper()).postDelayed({
            isReady = true
        }, 2000)

        // 4. Lanjutkan sisa setup UI seperti biasa
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
