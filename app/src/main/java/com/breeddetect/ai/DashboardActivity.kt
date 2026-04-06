package com.breeddetect.ai

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DashboardActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_dashboard)

        // Toolbar setup
        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        }

        // Floating Action Button
        val fabHelp: FloatingActionButton = findViewById(R.id.fabHelp)
        fabHelp.setOnClickListener {
            Toast.makeText(this, getString(R.string.help), Toast.LENGTH_SHORT).show()
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

    // Inflate menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    // Handle menu clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_language -> {
                showLanguageDialog()
                true
            }
            R.id.action_settings -> {
                showSettingsPopup()
                true
            }
            else -> super.onOptionsItemSelected(item)
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

    // Settings popup
    private fun showSettingsPopup() {
        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)

        val popup = PopupMenu(this, toolbar)
        popup.menuInflater.inflate(R.menu.settings_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings_option1 -> {
                    Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.settings_option2 -> {
                    Toast.makeText(this, "About clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }
}
