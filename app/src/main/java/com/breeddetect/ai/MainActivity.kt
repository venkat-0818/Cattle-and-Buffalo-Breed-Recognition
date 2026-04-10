package com.breeddetect.ai

import android.content.Intent
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class MainActivity : BaseActivity() {

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
    private lateinit var btnKnowMore: MaterialButton
    private lateinit var btnSave: MaterialButton
    private var lastPredictedBreed: String = ""
    private var lastConfidence: Int = 0

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

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
            try {
                val inputStream = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bitmap
                imageView.setImageBitmap(bitmap)
                tvNoImage.visibility = View.GONE
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
        btnKnowMore  = findViewById(R.id.btnKnowMore)
        btnSave      = findViewById(R.id.btnSave)

        btnKnowMore.setOnClickListener {
            if (lastPredictedBreed.isNotEmpty()) {
                val intent = Intent(this, WebViewActivity::class.java)
                intent.putExtra("breed", lastPredictedBreed)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Predict a breed first", Toast.LENGTH_SHORT).show()
            }
        }

        btnSave.setOnClickListener {
            savePredictionToFirestore()
        }

        // Load TFLite model and labels
        try {
            val options = Interpreter.Options()
            interpreter = Interpreter(loadModelFile("breed_classifier_fixed.tflite"), options)
            labels = assets.open("labels.txt")
                .bufferedReader()
                .readLines()
                .filter { it.isNotBlank() }
        } catch (e: Exception) {
            Log.e("TFLite", "Error loading model", e)
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

            progressBar.visibility = View.VISIBLE
            resultCard.visibility  = View.GONE

            Thread {
                val (breed, confidence) = predictImage(bitmap)
                
                // Save to local HistoryManager
                HistoryManager.add(breed, confidence, bitmap)

                runOnUiThread {
                    progressBar.visibility = View.GONE
                    resultCard.visibility  = View.VISIBLE
                    tvBreedName.text  = breed.replaceFirstChar { it.uppercase() }
                    tvConfidence.text = "Confidence: $confidence%"
                    lastPredictedBreed = breed
                    lastConfidence = confidence
                    btnSave.isEnabled = true
                }
            }.start()
        }
    }

    private fun savePredictionToFirestore() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please login to save", Toast.LENGTH_SHORT).show()
            return
        }

        if (lastPredictedBreed.isEmpty()) return

        btnSave.isEnabled = false
        val prediction = hashMapOf(
            "userId" to user.uid,
            "email" to user.email,
            "breed" to lastPredictedBreed,
            "confidence" to lastConfidence,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("saved_predictions")
            .add(prediction)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.prediction_saved), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                btnSave.isEnabled = true
                Toast.makeText(this, getString(R.string.failed_to_save) + ": ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(filename)
        val inputStream    = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel    = inputStream.channel
        val startOffset    = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    private fun predictImage(bitmap: Bitmap): Pair<String, Int> {
        val currentInterpreter = interpreter ?: return Pair("Model Error", 0)
        if (labels.isEmpty()) return Pair("Labels Error", 0)

        return try {
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val inputBuffer   = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
                .order(ByteOrder.nativeOrder())

            val intValues = IntArray(224 * 224)
            resizedBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

            for (pixelValue in intValues) {
                inputBuffer.putFloat(((pixelValue shr 16) and 0xFF).toFloat())
                inputBuffer.putFloat(((pixelValue shr 8) and 0xFF).toFloat())
                inputBuffer.putFloat((pixelValue and 0xFF).toFloat())
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
            Pair("Error: ${e.message}", 0)
        }
    }
}
