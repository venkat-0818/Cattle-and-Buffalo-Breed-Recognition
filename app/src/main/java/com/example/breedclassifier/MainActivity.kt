package com.example.breedclassifier

import android.content.Intent
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.History
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun BreedScreen() {
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
        var result by remember { mutableStateOf("No result") }
        var isLoading by remember { mutableStateOf(false) }

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

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("🐄 Breed Classifier") },
                    actions = {
                        // History button with live count badge
                        BadgedBox(
                            badge = {
                                if (HistoryManager.history.isNotEmpty()) {
                                    Badge {
                                        Text("${HistoryManager.history.size}")
                                    }
                                }
                            }
                        ) {
                            IconButton(onClick = {
                                startActivity(
                                    Intent(this@MainActivity, HistoryActivity::class.java)
                                )
                            }) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = "View History"
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Image Preview Box
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    bitmap?.let {
                        Image(it.asImageBitmap(), contentDescription = null)
                    } ?: Text(
                        "No image selected",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Camera & Gallery buttons
                Row {
                    Button(onClick = { cameraLauncher.launch(null) }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Camera")
                        Spacer(Modifier.width(6.dp))
                        Text("Camera")
                    }

                    Spacer(Modifier.width(20.dp))

                    Button(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery")
                        Spacer(Modifier.width(6.dp))
                        Text("Gallery")
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Predict button
                Button(
                    onClick = {
                        bitmap?.let { bmp ->
                            isLoading = true
                            lifecycleScope.launch {
                                val (breed, conf) = withContext(Dispatchers.Default) {
                                    predict(bmp)
                                }
                                // Update result text
                                result = "$breed ($conf%)"
                                // ✅ Save to history
                                HistoryManager.add(
                                    breed = breed,
                                    confidence = conf,
                                    bitmap = bmp
                                )
                                isLoading = false
                            }
                        } ?: Toast.makeText(
                            this@MainActivity,
                            "Please select an image first",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Predicting...")
                    } else {
                        Text("Predict Breed")
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Result Text
                if (result != "No result") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Result",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = result,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // View History button
                OutlinedButton(
                    onClick = {
                        startActivity(
                            Intent(this@MainActivity, HistoryActivity::class.java)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
//                    Text("View History (${HistoryManager.history.size})")
                    Text("View History")
                }
            }
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