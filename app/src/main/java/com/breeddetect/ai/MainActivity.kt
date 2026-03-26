package com.breeddetect.ai

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private var selectedBitmap: Bitmap? = null

    // Views
    private lateinit var imageView: ImageView
    private lateinit var tvNoImage: TextView
    private lateinit var tvBreedName: TextView
    private lateinit var tvConfidence: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var resultCard: MaterialCardView

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            selectedBitmap = it
            imageView.setImageBitmap(it)
            tvNoImage.visibility = View.GONE
        }
    }

    // Gallery launcher
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val inputStream = contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            selectedBitmap = bitmap
            imageView.setImageBitmap(bitmap)
            tvNoImage.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // added line
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                101
            )
        }

        // Toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { onBackPressed() }

        // Init views
        imageView    = findViewById(R.id.imageView)
        tvNoImage    = findViewById(R.id.tvNoImage)
        tvBreedName  = findViewById(R.id.tvBreedName)
        tvConfidence = findViewById(R.id.tvConfidence)
        progressBar  = findViewById(R.id.progressBar)
        resultCard   = findViewById(R.id.resultCard)

        // Load TFLite model and labels
        try {
            val options = Interpreter.Options()
            interpreter = Interpreter(loadModelFile("breed_classifier_fixed.tflite"), options)
            labels = assets.open("labels.txt")
                .bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }
            Log.d("TFLite", "Model and labels loaded: ${labels.size} classes")
        } catch (e: Exception) {
            Log.e("TFLite", "Error loading model", e)
            Toast.makeText(this, "Error loading model: ${e.message}", Toast.LENGTH_LONG).show()
        }

        // Camera button
        findViewById<MaterialCardView>(R.id.btnCamera).setOnClickListener {
            cameraLauncher.launch(null)
        }

        // Gallery button
        findViewById<MaterialCardView>(R.id.btnGallery).setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // Predict button
        findViewById<MaterialButton>(R.id.btnPredict).setOnClickListener {
            val bitmap = selectedBitmap
            if (bitmap == null) {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (interpreter == null || labels.isEmpty()) {
                Toast.makeText(this, "Model not loaded", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading
            progressBar.visibility = View.VISIBLE
            resultCard.visibility  = View.GONE

            // Run prediction in background thread
            Thread {
                val (breed, confidence) = predictImage(bitmap)
                savePrediction(breed)

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    resultCard.visibility  = View.VISIBLE
                    tvBreedName.text  = breed.replaceFirstChar { it.uppercase() }
                    tvConfidence.text = "Confidence: $confidence%"
                }
            }.start()
        }
    }

    // Load TFLite model from assets
    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(filename)
        val inputStream    = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel    = inputStream.channel
        val startOffset    = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Run inference
    private fun predictImage(bitmap: Bitmap): Pair<String, Int> {
        val currentInterpreter = interpreter ?: return Pair("Model Error", 0)
        if (labels.isEmpty()) return Pair("Labels Error", 0)

        return try {
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val inputBuffer   = ByteBuffer
                .allocateDirect(4 * 224 * 224 * 3)
                .order(ByteOrder.nativeOrder())

            val intValues = IntArray(224 * 224)
            resizedBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

            for (pixelValue in intValues) {
                // EfficientNet preprocessing — keep pixels in [0, 255]
                inputBuffer.putFloat(((pixelValue shr 16) and 0xFF).toFloat())
                inputBuffer.putFloat(((pixelValue shr 8)  and 0xFF).toFloat())
                inputBuffer.putFloat((pixelValue           and 0xFF).toFloat())
            }

            inputBuffer.rewind()

            val output = Array(1) { FloatArray(labels.size) }
            currentInterpreter.run(inputBuffer, output)

            val confidences = output[0]
            val maxIndex    = confidences.indices.maxByOrNull { confidences[it] } ?: -1
            val confidence  = (confidences[maxIndex] * 100).toInt()
            val breedName   = labels.getOrElse(maxIndex) { "Unknown" }

            Pair(breedName, confidence)
        } catch (e: Exception) {
            Log.e("TFLite", "Prediction error", e)
            Pair("Error: ${e.message}", 0)
        }
    }

    // Save prediction to history
    private fun savePrediction(predictedBreed: String) {
        val sharedPrefs = getSharedPreferences("breed_prefs", MODE_PRIVATE)
        val editor      = sharedPrefs.edit()
        val gson        = Gson()
        val json        = sharedPrefs.getString("prediction_history", null)
        val type        = object : TypeToken<MutableList<String>>() {}.type
        val history: MutableList<String> = if (json != null) gson.fromJson(json, type)
        else mutableListOf()
        history.add(predictedBreed)
        editor.putString("prediction_history", gson.toJson(history))
        editor.apply()
    }
}
