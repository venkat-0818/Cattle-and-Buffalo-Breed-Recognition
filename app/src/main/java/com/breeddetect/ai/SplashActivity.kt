package com.breeddetect.ai

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val lang = prefs.getString("lang", "en") ?: "en"
        LocaleHelper.setLocale(this, lang)
        
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && user.isEmailVerified) {
                // User is already logged in and verified, go to Dashboard
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                // No user or not verified, go to Login
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
