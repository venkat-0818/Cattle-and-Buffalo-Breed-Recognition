package com.example.breedclassifier

object HistoryManager {
    val history = mutableListOf<PredictionHistory>()

    fun add(breed: String, confidence: Int, bitmap: android.graphics.Bitmap?) {
        history.add(0, PredictionHistory(
            breed = breed,
            confidence = confidence,
            bitmap = bitmap
        ))
    }

    fun delete(id: Long) {
        history.removeAll { it.id == id }
    }

    fun clear() {
        history.clear()
    }
}