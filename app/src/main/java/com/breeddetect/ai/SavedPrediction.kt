package com.breeddetect.ai

data class SavedPrediction(
    val id: String = "",
    val userId: String = "",
    val breed: String = "",
    val confidence: Int = 0,
    val timestamp: Long = 0,
    val imageUrl: String = "" // Optional: if you want to store in Firebase Storage
)
