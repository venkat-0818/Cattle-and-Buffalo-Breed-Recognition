package com.breeddetect.ai

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginBtn: Button
    private lateinit var goToRegister: TextView
    private lateinit var forgotPasswordBtn: TextView
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginBtn = findViewById(R.id.loginBtn)
        goToRegister = findViewById(R.id.goToRegister)
        forgotPasswordBtn = findViewById(R.id.forgotPasswordBtn)

        mAuth = FirebaseAuth.getInstance()

        loginBtn.setOnClickListener { loginUser() }

        goToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        forgotPasswordBtn.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.forgot_password))
        
        val input = EditText(this)
        input.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        input.hint = getString(R.string.email)
        
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        
        val container = LinearLayout(this)
        container.orientation = LinearLayout.VERTICAL
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(50, 20, 50, 0)
        container.addView(input, params)
        
        builder.setView(container)

        builder.setPositiveButton(getString(R.string.send)) { _, _ ->
            val resetEmail = input.text.toString().trim()
            if (TextUtils.isEmpty(resetEmail)) {
                Toast.makeText(this, getString(R.string.please_enter_email), Toast.LENGTH_SHORT).show()
            } else {
                sendPasswordResetEmail(resetEmail)
            }
        }
        builder.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun sendPasswordResetEmail(resetEmail: String) {
        mAuth.sendPasswordResetEmail(resetEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.password_reset_sent), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun loginUser() {
        val userEmail = email.text.toString().trim()
        val userPass = password.text.toString().trim()

        if (TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPass)) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.signInWithEmailAndPassword(userEmail, userPass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val user = mAuth.currentUser

                    if (user != null && user.isEmailVerified) {
                        // After successful login, go to DashboardActivity, NOT SplashActivity
                        startActivity(Intent(this, DashboardActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Verify your email first", Toast.LENGTH_LONG).show()
                    }

                } else {
                    Toast.makeText(
                        this,
                        task.exception?.message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}
