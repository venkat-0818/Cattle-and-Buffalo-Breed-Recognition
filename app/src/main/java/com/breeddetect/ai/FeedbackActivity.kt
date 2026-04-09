package com.breeddetect.ai

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackActivity : AppCompatActivity() {

    lateinit var feedbackEdit: EditText
    lateinit var submitBtn: Button

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        feedbackEdit = findViewById(R.id.feedbackEdit)
        submitBtn = findViewById(R.id.submitBtn)

        submitBtn.setOnClickListener {

            val feedback = feedbackEdit.text.toString()

            if (feedback.isEmpty()) {
                Toast.makeText(this, "Enter feedback", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val data = hashMapOf(
                "feedback" to feedback,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("feedback")
                .add(data)
                .addOnSuccessListener {

                    Toast.makeText(this, "Feedback Submitted", Toast.LENGTH_LONG).show()
                    feedbackEdit.setText("")

                }
                .addOnFailureListener {

                    Toast.makeText(this, "Error saving feedback", Toast.LENGTH_LONG).show()
                }
        }
    }
}