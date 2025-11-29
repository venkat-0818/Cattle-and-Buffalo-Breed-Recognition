
package com.example.breedclassifier

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DashboardActivity : AppCompatActivity() {

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
            Toast.makeText(this, "Help clicked", Toast.LENGTH_SHORT).show()
        }

        // Card: Scan / Identify Breed → Open MainActivity
        findViewById<MaterialCardView>(R.id.cardScanBreed).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        // Card: Prediction History → Open HistoryActivity
        findViewById<MaterialCardView>(R.id.cardViewHistory).setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    // Inflate the toolbar menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.dashboard_menu, menu)
        return true
    }

    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                showSettingsPopup() // <-- Show popup on same screen
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showSettingsPopup() {
        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)

        // Anchor the popup to the toolbar itself
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


//package com.example.breedclassifier
//
//import android.content.Intent
//import android.os.Bundle
//import android.view.Menu
//import android.view.MenuItem
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.google.android.material.appbar.MaterialToolbar
//import com.google.android.material.card.MaterialCardView
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//
//class DashboardActivity : AppCompatActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main_dashboard)
//
//        // Setup Toolbar
//        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
//        setSupportActionBar(toolbar)
//
//        toolbar.setNavigationOnClickListener {
//            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
//        }
//
//        // Floating Action Button
//        val fabHelp: FloatingActionButton = findViewById(R.id.fabHelp)
//        fabHelp.setOnClickListener {
//            Toast.makeText(this, "Help clicked", Toast.LENGTH_SHORT).show()
//        }
//
//        // Card: Scan / Identify Breed → Open MainActivity
//        findViewById<MaterialCardView>(R.id.cardScanBreed).setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }
//
//        // Card: Prediction History → Toast
//        findViewById<MaterialCardView>(R.id.cardViewHistory).setOnClickListener {
//            val intent = Intent(this, HistoryActivity::class.java)
//            startActivity(intent)
//        }
//
//    }
//
//    // Inflate the toolbar menu
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.dashboard_menu, menu)
//        return true
//    }
//
//    // Handle menu item clicks
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.action_settings -> {
//                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
//}