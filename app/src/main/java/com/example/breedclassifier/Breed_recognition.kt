package com.example.breedclassifier

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class Breed_recognition : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var interpreter: Interpreter
    private lateinit var labels: List<String>
    private var selectedBitmap: Bitmap? = null

    companion object {
        private const val IMAGE_PICK_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breed_recognition)

        imageView = findViewById(R.id.imageView)
        resultText = findViewById(R.id.resultText)
        val btnSelectImage: Button = findViewById(R.id.btnSelectImage)
        val btnPredict: Button = findViewById(R.id.btnPredict)

        try {
            interpreter = Interpreter(loadModelFile("breed_model.tflite"))
            labels = assets.open("labels.txt").bufferedReader().readLines()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading model or labels", Toast.LENGTH_LONG).show()
        }

        btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, IMAGE_PICK_CODE)
        }

        btnPredict.setOnClickListener {
            selectedBitmap?.let {
                val (breed, conf) = predictImage(it)
                resultText.text = "Predicted: $breed\nConfidence: $conf%"
            } ?: run {
                Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            }
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
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val inputBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3).order(ByteOrder.nativeOrder())

        val intValues = IntArray(224 * 224)
        resizedBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)

        var pixel = 0
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val value = intValues[pixel++]
                inputBuffer.putFloat(((value shr 16 and 0xFF) / 255.0f))
                inputBuffer.putFloat(((value shr 8 and 0xFF) / 255.0f))
                inputBuffer.putFloat(((value and 0xFF) / 255.0f))
            }
        }

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(inputBuffer, output)

        val confidences = output[0]
        val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1
        val confidence = (confidences[maxIndex] * 100).toInt()
        val breedName = labels[maxIndex]

        return Pair(breedName, confidence)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            try {
                val inputStream = contentResolver.openInputStream(imageUri!!)
                selectedBitmap = BitmapFactory.decodeStream(inputStream)
                imageView.setImageBitmap(selectedBitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
