package com.breeddetect.ai

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val user = mAuth.currentUser

        val tvUserEmail: TextView = findViewById(R.id.tvUserEmail)
        val tvDetailEmail: TextView = findViewById(R.id.tvDetailEmail)
        val btnLogout: MaterialButton = findViewById(R.id.btnLogout)
        val btnDeleteAccount: MaterialButton = findViewById(R.id.btnDeleteAccount)
        val toolbar: MaterialToolbar = findViewById(R.id.profileToolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        if (user != null) {
            tvUserEmail.text = user.email
            tvDetailEmail.text = user.email
        }

        btnLogout.setOnClickListener {
            mAuth.signOut()
            navigateToLogin()
        }

        btnDeleteAccount.setOnClickListener {
            showDeleteAccountConfirmation()
        }
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_account)
            .setMessage(R.string.delete_account_confirm)
            .setPositiveButton(R.string.delete_account) { _, _ ->
                deleteAccount()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun deleteAccount() {
        val user = mAuth.currentUser ?: return
        val userId = user.uid

        // 1. Delete Firestore Data
        db.collection("saved_predictions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val batch = db.batch()
                for (document in documents) {
                    batch.delete(document.reference)
                }
                batch.commit().addOnCompleteListener {
                    // 2. Delete Auth Account
                    user.delete()
                        .addOnSuccessListener {
                            // 3. Clear Local Data
                            HistoryManager.clear()
                            
                            Toast.makeText(this, R.string.account_deleted, Toast.LENGTH_SHORT).show()
                            navigateToLogin()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "${getString(R.string.failed_delete_account)}: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
