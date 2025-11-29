
package com.example.breedclassifier

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : ComponentActivity() {

    private lateinit var interpreter: Interpreter
    private lateinit var labels: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

         fun savePrediction(predictedBreed: String) {
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

            // Add new prediction
            history.add(predictedBreed)

            // Save updated history
            editor.putString("prediction_history", gson.toJson(history))
            editor.apply()
        }


        // Load TFLite model and labels
        try {
            interpreter = Interpreter(loadModelFile("breed_classifier_model.tflite"))
            labels =
                assets.open("labels.txt").bufferedReader().readLines().filter { it.isNotBlank() }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading model or labels", Toast.LENGTH_LONG).show()
        }

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BreedClassifierScreen()
                }
            }
        }
    }

    @Composable
    fun BreedClassifierScreen() {
        var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var resultText by remember { mutableStateOf("Upload or capture an image to begin") }
        var isLoading by remember { mutableStateOf(false) }

        // 📸 Camera launcher
        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bitmap: Bitmap? ->
            bitmap?.let { selectedBitmap = it }
        }

        // 🖼️ Gallery launcher
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

            // 🖼️ Image preview box
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (selectedBitmap != null) {
                    Image(bitmap = selectedBitmap!!.asImageBitmap(), contentDescription = "Selected Image")
                } else {
                    Text(
                        text = "No Image Selected",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ⚙️ Camera & Gallery Buttons (side-by-side)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Camera button
                Button(
                    onClick = { cameraLauncher.launch(null) },
                    shape = CircleShape,
                    modifier = Modifier.size(70.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo")
                }

                // Gallery button
                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    shape = CircleShape,
                    modifier = Modifier.size(70.dp)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Select Image")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔮 Predict button
            Button(
                onClick = {
                    if (selectedBitmap == null) {
                        Toast.makeText(
                            this@MainActivity,
                            "Please select or capture an image first",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }

                    isLoading = true
                    resultText = "Processing..."

                    lifecycleScope.launch {
                        val (breed, conf) = withContext(Dispatchers.Default) {
                            predictImage(selectedBitmap!!)
                        }
                        resultText = "Predicted: $breed\nConfidence: $conf%"
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

    /** Load model from assets */
    private fun loadModelFile(filename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(filename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }



    /** Run inference */
    private fun predictImage(bitmap: Bitmap): Pair<String, Int> {
        return try {
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            val bmp = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val inputBuffer =
                ByteBuffer.allocateDirect(4 * 224 * 224 * 3).order(ByteOrder.nativeOrder())

            val intValues = IntArray(224 * 224)
            bmp.getPixels(intValues, 0, 224, 0, 0, 224, 224)

            for (y in 0 until 224) {
                for (x in 0 until 224) {
                    val value = intValues[y * 224 + x]
                    inputBuffer.putFloat(((value shr 16) and 0xFF) / 255f)
                    inputBuffer.putFloat(((value shr 8) and 0xFF) / 255f)
                    inputBuffer.putFloat((value and 0xFF) / 255f)
                }
            }

            inputBuffer.rewind()

            val output = Array(1) { FloatArray(labels.size) }
            interpreter.run(inputBuffer, output)

            val confidences = output[0]
            val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1
            val confidence = (confidences[maxIndex] * 100).toInt()
            val breedName = labels[maxIndex]

            Pair(breedName, confidence)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair("Error", 0)
        }
    }
}


//package com.example.breedclassifier
//
//import android.graphics.Bitmap
//import android.graphics.BitmapFactory
//import android.net.Uri
//import android.os.Bundle
//import android.widget.Toast
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.lifecycleScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import org.tensorflow.lite.Interpreter
//import java.io.FileInputStream
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//import java.nio.MappedByteBuffer
//import java.nio.channels.FileChannel
//
//class MainActivity : ComponentActivity() {
//
//    private lateinit var interpreter: Interpreter
//    private lateinit var labels: List<String>
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Load TFLite model and labels
//        try {
//            interpreter = Interpreter(loadModelFile("breed_classifier_model.tflite"))
//            labels =
//                assets.open("labels.txt").bufferedReader().readLines().filter { it.isNotBlank() }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(this, "Error loading model or labels", Toast.LENGTH_LONG).show()
//        }
//
//        setContent {
//            BreedClassifierScreen()
//        }
//    }
//
//    @Composable
//    fun BreedClassifierScreen() {
//        var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
//        var resultText by remember { mutableStateOf("Result will appear here") }
//        var isLoading by remember { mutableStateOf(false) }
//
//        // Launcher to pick image from gallery
//        val launcher = rememberLauncherForActivityResult(
//            contract = ActivityResultContracts.GetContent()
//        ) { uri: Uri? ->
//            uri?.let {
//                val inputStream = contentResolver.openInputStream(it)
//                selectedBitmap = BitmapFactory.decodeStream(inputStream)
//            }
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Top
//        ) {
//            Text(
//                text = "🐄 Breed Identification",
//                fontSize = 24.sp,
//                modifier = Modifier.padding(bottom = 24.dp)
//            )
//
//            Box(
//                modifier = Modifier
//                    .size(280.dp)
//                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
//                contentAlignment = Alignment.Center
//            ) {
//                selectedBitmap?.let {
//                    Image(bitmap = it.asImageBitmap(), contentDescription = "Selected Image")
//                }
//            }
//
//            Spacer(modifier = Modifier.height(20.dp))
//
//            Button(onClick = { launcher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
//                Text("Select Image")
//            }
//
//            Spacer(modifier = Modifier.height(12.dp))
//
//            Button(
//                onClick = {
//                    if (selectedBitmap == null) {
//                        Toast.makeText(
//                            this@MainActivity,
//                            "Please select an image first",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        return@Button
//                    }
//
//                    isLoading = true
//                    resultText = "Processing..."
//
//                    // Run TFLite prediction in background
//                    lifecycleScope.launch {
//                        val (breed, conf) = withContext(Dispatchers.Default) {
//                            predictImage(selectedBitmap!!)
//                        }
//                        resultText = "Predicted: $breed\nConfidence: $conf%"
//                        isLoading = false
//                    }
//                },
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Text("Predict Breed")
//            }
//
//            Spacer(modifier = Modifier.height(20.dp))
//
//            if (isLoading) {
//                CircularProgressIndicator()
//                Spacer(modifier = Modifier.height(12.dp))
//                Text("Processing...", fontSize = 16.sp)
//            }
//
//            Text(text = resultText, fontSize = 18.sp)
//        }
//    }
//
//    private fun loadModelFile(filename: String): MappedByteBuffer {
//        val fileDescriptor = assets.openFd(filename)
//        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
//        val fileChannel = inputStream.channel
//        val startOffset = fileDescriptor.startOffset
//        val declaredLength = fileDescriptor.declaredLength
//        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
//    }
//
//    private fun predictImage(bitmap: Bitmap): Pair<String, Int> {
//        return try {
//            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
//            val bmp = resizedBitmap.copy(Bitmap.Config.ARGB_8888, true)
//            val inputBuffer =
//                ByteBuffer.allocateDirect(4 * 224 * 224 * 3).order(ByteOrder.nativeOrder())
//
//            val intValues = IntArray(224 * 224)
//            bmp.getPixels(intValues, 0, 224, 0, 0, 224, 224)
//
//    //            var pixel = 0
//            for (y in 0 until 224) {
//                for (x in 0 until 224) {
//                    val value = intValues[y * 224 + x]
//                    inputBuffer.putFloat(((value shr 16) and 0xFF) / 255f) // R
//                    inputBuffer.putFloat(((value shr 8) and 0xFF) / 255f)  // G
//                    inputBuffer.putFloat((value and 0xFF) / 255f)          // B
//                }
//            }
//
//
//            // ✅ Important: reset buffer position before running inference
//            inputBuffer.rewind()
//
//            val output = Array(1) { FloatArray(labels.size) }
//            interpreter.run(inputBuffer, output)
//
//            val confidences = output[0]
//            val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1
//            val confidence = confidences[maxIndex] * 100f
//
//            val breedName = labels[maxIndex]
//
//            Pair(breedName, confidence)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Pair("Error", 0)
//        } as Pair<String, Int>
//    }
//}