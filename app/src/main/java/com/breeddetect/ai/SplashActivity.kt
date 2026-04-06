package com.breeddetect.ai

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
        val lang = prefs.getString("lang", "en") ?: "en"
        LocaleHelper.setLocale(this, lang)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({

            startActivity(Intent(this, DashboardActivity::class.java))
            finish()

        }, 2000)

    }
}