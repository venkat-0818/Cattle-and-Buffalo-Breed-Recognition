package com.example.breedclassifier

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
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
import java.nio.channels.FileChannel

class MainActivity : ComponentActivity() {

    private lateinit var interpreter: Interpreter
    private lateinit var labels: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            interpreter = Interpreter(loadModelFile("breed_classifier_model.tflite"))
            labels = assets.open("labels.txt").bufferedReader().readLines()
        } catch (e: Exception) {
            Toast.makeText(this, "Model load error", Toast.LENGTH_LONG).show()
        }

        setContent {
            MaterialTheme {
                BreedScreen()
            }
        }
    }

    @Composable
    fun BreedScreen() {

        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
        var result by remember { mutableStateOf("No result") }

        val cameraLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicturePreview()
        ) { bmp -> bitmap = bmp }

        val galleryLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                val stream = contentResolver.openInputStream(it)
                bitmap = BitmapFactory.decodeStream(stream)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("🐄 Breed Classifier", fontSize = 24.sp)

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier.size(250.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                bitmap?.let {
                    Image(it.asImageBitmap(), contentDescription = null)
                }
            }

            Spacer(Modifier.height(20.dp))

            Row {
                Button(onClick = { cameraLauncher.launch(null) }) {
                    Icon(Icons.Default.CameraAlt, null)
                }

                Spacer(Modifier.width(20.dp))

                Button(onClick = { galleryLauncher.launch("image/*") }) {
                    Icon(Icons.Default.PhotoLibrary, null)
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(onClick = {
                bitmap?.let {
                    lifecycleScope.launch {
                        val (breed, conf) = withContext(Dispatchers.Default) {
                            predict(it)
                        }
                        result = "$breed ($conf%)"
                    }
                }
            }) {
                Text("Predict")
            }

            Spacer(Modifier.height(20.dp))
            Text(result)
        }
    }

    private fun loadModelFile(name: String): ByteBuffer {
        val file = assets.openFd(name)
        val input = FileInputStream(file.fileDescriptor)
        return input.channel.map(FileChannel.MapMode.READ_ONLY, file.startOffset, file.declaredLength)
    }

    private fun predict(bitmap: Bitmap): Pair<String, Int> {
        val resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val buffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3).order(ByteOrder.nativeOrder())

        val pixels = IntArray(224 * 224)
        resized.getPixels(pixels, 0, 224, 0, 0, 224, 224)

        for (p in pixels) {
            buffer.putFloat(((p shr 16) and 0xFF) / 255f)
            buffer.putFloat(((p shr 8) and 0xFF) / 255f)
            buffer.putFloat((p and 0xFF) / 255f)
        }

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(buffer, output)

        val index = output[0].indices.maxByOrNull { output[0][it] } ?: 0
        return labels[index] to (output[0][index] * 100).toInt()
    }
}