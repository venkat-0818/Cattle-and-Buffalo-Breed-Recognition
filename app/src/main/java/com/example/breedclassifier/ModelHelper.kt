package com.example.breedclassifier

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.min

class ModelHelper(context: Context) {

    private val interpreter: Interpreter
    private val labels = listOf(
        "Jersey Cattle",
        "Holstein Cattle",
        "Murrah Buffalo",
        "Jaffarabadi Buffalo",
        "ongole cattle",
        "Other Breed"
    ) // Change as per your dataset

    init {
        val tfliteModel = loadModelFile(context, "breed_classifier.tflite")
        interpreter = Interpreter(tfliteModel)
    }

    private fun loadModelFile(context: Context, modelFileName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelFileName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predict(bitmap: Bitmap): Pair<String, Float> {
        val inputBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val byteBuffer = convertBitmapToByteBuffer(inputBitmap)
        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(byteBuffer, output)

        val maxIndex = output[0].indices.maxByOrNull { output[0][it] } ?: -1
        val confidence = if (maxIndex != -1) output[0][maxIndex] else 0f
        val breedName = if (maxIndex != -1) labels[maxIndex] else "Unknown"

        return breedName to confidence
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(224 * 224)
        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
        for (pixel in intValues) {
            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
            byteBuffer.putFloat((pixel and 0xFF) / 255f)
        }
        return byteBuffer
    }
}
