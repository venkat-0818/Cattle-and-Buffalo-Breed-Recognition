package com.breeddetect.ai

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginBtn: Button
    private lateinit var goToRegister: TextView
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginBtn = findViewById(R.id.loginBtn)
        goToRegister = findViewById(R.id.goToRegister)

        mAuth = FirebaseAuth.getInstance()

        loginBtn.setOnClickListener { loginUser() }

        goToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
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
