package com.breeddetect.ai

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_dashboard)

        drawerLayout = findViewById(R.id.drawerLayout)
        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        val navigationView: NavigationView = findViewById(R.id.navigationView)

        setSupportActionBar(toolbar)

        // Open drawer on hamburger icon click
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle Navigation Drawer item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
                R.id.nav_saved -> {
                    startActivity(Intent(this, SavedActivity::class.java))
                }
                R.id.nav_language -> {
                    showLanguageDialog()
                }
                R.id.nav_feedback -> {
                    startActivity(Intent(this, FeedbackActivity::class.java))
                }
                R.id.nav_about -> {
                    startActivity(Intent(this, AboutActivity::class.java))
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Floating Action Button
        val fabHelp: FloatingActionButton = findViewById(R.id.fabHelp)
        fabHelp.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Card: Scan
        findViewById<MaterialCardView>(R.id.cardScanBreed).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Card: History
        findViewById<MaterialCardView>(R.id.cardViewHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    // Language Dialog
    private fun showLanguageDialog() {
        val languages = arrayOf("English", "हिंदी", "తెలుగు", "ਪੰਜਾਬੀ")
        val codes = arrayOf("en", "hi", "te", "pa")

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_language))

        builder.setItems(languages) { _, which ->
            val selectedLang = codes[which]

            val prefs = getSharedPreferences("Settings", MODE_PRIVATE)
            prefs.edit().putString("lang", selectedLang).apply()

            // Restart activity to apply language changes
            val intent = Intent(this, SplashActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }

        builder.show()
    }
}