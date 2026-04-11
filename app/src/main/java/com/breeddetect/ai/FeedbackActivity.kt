package com.breeddetect.ai

import android.os.Bundle
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FeedbackActivity : AppCompatActivity() {

    private lateinit var feedbackEdit: TextInputEditText
    private lateinit var submitBtn: MaterialButton
    private lateinit var ratingBar: RatingBar
    private lateinit var tvRatingStatus: TextView
    private lateinit var toolbar: MaterialToolbar

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        // Initialize Views
        toolbar = findViewById(R.id.feedbackToolbar)
        feedbackEdit = findViewById(R.id.feedbackEdit)
        submitBtn = findViewById(R.id.submitBtn)
        ratingBar = findViewById(R.id.ratingBar)
        tvRatingStatus = findViewById(R.id.tvRatingStatus)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        // RatingBar Listener
        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, rating, _ ->
            updateRatingStatus(rating)
        }

        submitBtn.setOnClickListener {
            val feedback = feedbackEdit.text.toString().trim()
            val rating = ratingBar.rating

            if (rating == 0f) {
                Toast.makeText(this, getString(R.string.please_rate), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            submitFeedback(rating, feedback)
        }
    }

    private fun updateRatingStatus(rating: Float) {
        val status = when (rating.toInt()) {
            1 -> getString(R.string.rating_poor)
            2 -> getString(R.string.rating_fair)
            3 -> getString(R.string.rating_good)
            4 -> getString(R.string.rating_very_good)
            5 -> getString(R.string.rating_excellent)
            else -> getString(R.string.tap_to_rate)
        }
        tvRatingStatus.text = status
    }

    private fun submitFeedback(rating: Float, comment: String) {
        submitBtn.isEnabled = false
        val user = auth.currentUser
        
        val feedbackData = hashMapOf(
            "userId" to (user?.uid ?: "anonymous"),
            "userEmail" to (user?.email ?: "anonymous"),
            "rating" to rating,
            "comment" to comment,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("feedback")
            .add(feedbackData)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.feedback_submitted), Toast.LENGTH_LONG).show()
                finish() // Close activity after success
            }
            .addOnFailureListener { e ->
                submitBtn.isEnabled = true
                Toast.makeText(this, "${getString(R.string.error_saving_feedback)}: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
