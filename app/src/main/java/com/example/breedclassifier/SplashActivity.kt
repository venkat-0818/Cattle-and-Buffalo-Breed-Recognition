package com.example.breedclassifier

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logoImage)
        val appName = findViewById<TextView>(R.id.appName)
        val tagline = findViewById<TextView>(R.id.tagline)

        // Ensure initial alpha = 0
        logo.alpha = 0f
        appName.alpha = 0f
        tagline.alpha = 0f

        // Animate logo
        logo.animate()
            .alpha(1f)
            .setDuration(1200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Animate text after a delay
        lifecycleScope.launch {
            delay(1000)
            appName.animate()
                .alpha(1f)
                .setDuration(800)
                .start()

            tagline.animate()
                .alpha(1f)
                .translationYBy(-30f)
                .setDuration(1000)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            // Wait before launching Dashboard
            delay(1500)
            startActivity(Intent(this@SplashActivity, DashboardActivity::class.java))
            finish()
        }
    }
}
