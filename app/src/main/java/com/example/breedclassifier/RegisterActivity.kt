package com.example.breedclassifier

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var registerBtn: Button
    private lateinit var goToLogin: TextView
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        registerBtn = findViewById(R.id.registerBtn)
        goToLogin = findViewById(R.id.goToLogin)

        mAuth = FirebaseAuth.getInstance()

        registerBtn.setOnClickListener { registerUser() }

        goToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser() {
        val userEmail = email.text.toString().trim()
        val userPass = password.text.toString().trim()

        if (TextUtils.isEmpty(userEmail) || TextUtils.isEmpty(userPass)) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        mAuth.createUserWithEmailAndPassword(userEmail, userPass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val user = mAuth.currentUser

                    user?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            Toast.makeText(this, "Verification email sent", Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }

                } else {
                    Toast.makeText(
                        this,
                        "Error: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}