
package com.example.breedclassifier

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.nnapi.NnApiDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : ComponentActivity() {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Load TFLite model and labels
        try {
            val options = Interpreter.Options()
            // Some models require Flex delegate or NNAPI for newer op versions
            // options.addDelegate(NnApiDelegate()) 

            interpreter = Interpreter(loadModelFile("breed_classifier_fixed.tflite"), options)
            labels =
                assets.open("labels.txt").bufferedReader().readLines().filter { it.isNotBlank() }
            Log.d("TFLite", "Model and labels loaded successfully")
        } catch (e: Exception) {
            Log.e("TFLite", "Error loading model", e)
            Toast.makeText(this, "Error loading model: ${e.message}", Toast.LENGTH_LONG).show()
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BreedClassifierScreen()
                }
            }
        }
    }

    private fun savePrediction(predictedBreed: String) {
        val sharedPrefs = getSharedPreferences("breed_prefs", MODE_PRIVATE)
        val editor = sharedPrefs.edit()

        val gson = Gson()
        val json = sharedPrefs.getString("prediction_history", null)
        val type = object : TypeToken<MutableList<String>>() {}.type
        val history: MutableList<String> = if (json != null) {
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }

        history.add(predictedBreed)
        editor.putString("prediction_history", gson.toJson(history))
        editor.apply()
    }

    @Composable
    fun BreedClassifierScreen() {
        var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var resultText by remember { mutableStateOf("Upload or capture an image to begin") }
        var isLoading by remember { mutableStateOf(false) }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bitmap: Bitmap? ->
            bitmap?.let { selectedBitmap = it }
        }

        val galleryLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                val inputStream = contentResolver.openInputStream(it)
                selectedBitmap = BitmapFactory.decodeStream(inputStream)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🐄 BoviScan",
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "AI-Powered Breed Identification",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selectedBitmap != null) {
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "No Image Selected",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    shape = CircleShape,
                    modifier = Modifier.size(70.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo")
                }

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    shape = CircleShape,
                    modifier = Modifier.size(70.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Select Image")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedBitmap == null) {
                        Toast.makeText(
                            this@MainActivity,
                            "Please select an image first",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    if (interpreter == null || labels.isEmpty()) {
                        Toast.makeText(this@MainActivity, "Model not loaded", Toast.LENGTH_SHORT)
                            .show()
                        return@Button
                    }

                    isLoading = true
                    resultText = "Processing..."

                    lifecycleScope.launch {
                        val result = withContext(Dispatchers.Default) {
                            predictImage(selectedBitmap!!)
                        }
                        resultText = "Predicted: ${result.first}\nConfidence: ${result.second}%"
                        savePrediction(result.first)
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Predict Breed", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(text = resultText, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }

    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }


    private fun predictImage(bitmap: Bitmap): Pair<String, Int> {
        val currentInterpreter = interpreter ?: return Pair("Model Error", 0)
        val currentLabels = labels
        if (currentLabels.isEmpty()) return Pair("Labels Error", 0)

        return try {
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val inputBuffer =
                ByteBuffer.allocateDirect(4 * 224 * 224 * 3).order(ByteOrder.nativeOrder())

            val intValues = IntArray(224 * 224)
            resizedBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

            for (pixelValue in intValues) {

                inputBuffer.putFloat(((pixelValue shr 16) and 0xFF).toFloat())
                inputBuffer.putFloat(((pixelValue shr 8)  and 0xFF).toFloat())
                inputBuffer.putFloat((pixelValue           and 0xFF).toFloat())
            }

            inputBuffer.rewind()
            val output = Array(1) { FloatArray(currentLabels.size) }
            currentInterpreter.run(inputBuffer, output)

            val confidences = output[0]
            val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1
            val confidence = (confidences[maxIndex] * 100).toInt()
            val breedName = currentLabels.getOrElse(maxIndex) { "Unknown" }

            Pair(breedName, confidence)
        } catch (e: Exception) {
            Log.e("TFLite", "Prediction error", e)
            Pair("Error: ${e.message}", 0)
        }
    }
}
