package com.breeddetect.ai

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SavedActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavedAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmptyState: TextView
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved)

        val toolbar: MaterialToolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        recyclerView = findViewById(R.id.rvSavedPredictions)
        progressBar = findViewById(R.id.progressBar)
        tvEmptyState = findViewById(R.id.tvEmptyState)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SavedAdapter(emptyList()) { predictionId ->
            deletePrediction(predictionId)
        }
        recyclerView.adapter = adapter

        fetchSavedPredictions()
    }

    private fun fetchSavedPredictions() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please login to view saved predictions", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        progressBar.visibility = View.VISIBLE
        db.collection("saved_predictions")
            .whereEqualTo("userId", user.uid)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
                progressBar.visibility = View.GONE
                if (error != null) {
                    Toast.makeText(this, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val savedList = value?.map { doc ->
                    val prediction = doc.toObject(SavedPrediction::class.java)
                    prediction.copy(id = doc.id)
                } ?: emptyList()

                if (savedList.isEmpty()) {
                    tvEmptyState.visibility = View.VISIBLE
                    tvEmptyState.text = getString(R.string.no_saved_predictions)
                } else {
                    tvEmptyState.visibility = View.GONE
                }

                adapter.updateData(savedList)
            }
    }

    private fun deletePrediction(id: String) {
        db.collection("saved_predictions").document(id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error deleting: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
