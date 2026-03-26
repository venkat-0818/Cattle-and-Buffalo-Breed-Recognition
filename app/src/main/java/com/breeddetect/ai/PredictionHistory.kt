package com.breeddetect.ai

import android.graphics.Bitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PredictionHistory(
    val id: Long = System.currentTimeMillis(),
    val breed: String,
    val confidence: Int,
    val bitmap: Bitmap?,
    val timestamp: String = SimpleDateFormat(
        "dd MMM yyyy, hh:mm a", Locale.getDefault()
    ).format(Date())
)