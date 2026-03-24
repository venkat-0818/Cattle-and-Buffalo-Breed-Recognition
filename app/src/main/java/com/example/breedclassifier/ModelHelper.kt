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
        "alambadi",
        "amritmahal",
        "ayrshire",
        "banni",
        "bargur",
        "bhadawari",
        "brown_swiss",
        "deoni",
        "gir",
        "guernsey",
        "hallikar",
        "holstein-friesian",
        "jaffarabadi",
        "mehsana",
        "murrah",
        "nagpuri",
        "nili_ravi",
        "sahiwal",
        "tharparkar",
        "toda"
    )

    init {
        val tfliteModel = loadModelFile(context, "breed_classifier_fixed.tflite")
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
            val r = ((pixel shr 16) and 0xFF)
            val g = ((pixel shr 8) and 0xFF)
            val b = (pixel and 0xFF)

            // 🔥 FIXED NORMALIZATION
            byteBuffer.putFloat((r - 127.5f) / 127.5f)
            byteBuffer.putFloat((g - 127.5f) / 127.5f)
            byteBuffer.putFloat((b - 127.5f) / 127.5f)
        }

        return byteBuffer
    }

//    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
//        val byteBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)
//        byteBuffer.order(ByteOrder.nativeOrder())
//        val intValues = IntArray(224 * 224)
//        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
//        for (pixel in intValues) {
//            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255f)
//            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255f)
//            byteBuffer.putFloat((pixel and 0xFF) / 255f)
//        }
//        return byteBuffer
//    }
}


//package com.example.breedclassifier
//
//import android.content.Context
//import android.graphics.Bitmap
//import org.tensorflow.lite.Interpreter
//import java.io.FileInputStream
//import java.nio.ByteBuffer
//import java.nio.ByteOrder
//import java.nio.MappedByteBuffer
//import java.nio.channels.FileChannel
//import kotlin.math.exp
//
//class ModelHelper(context: Context) {
//
//    private val interpreter: Interpreter
//
//    private val labels = listOf(
//        "alambadi","amritmahal","ayrshire","banni","bargur","bhadawari",
//        "brown_swiss","deoni","gir","guernsey","hallikar","holstein-friesian",
//        "jaffarabadi","mehsana","murrah","nagpuri","nili_ravi","sahiwal",
//        "tharparkar","toda"
//    )
//
//    init {
//        val tfliteModel = loadModelFile(context, "breed_classifier_fixed.tflite")
//        interpreter = Interpreter(tfliteModel)
//    }
//
//    private fun loadModelFile(context: Context, modelFileName: String): MappedByteBuffer {
//        val fileDescriptor = context.assets.openFd(modelFileName)
//        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
//        val fileChannel = inputStream.channel
//        return fileChannel.map(
//            FileChannel.MapMode.READ_ONLY,
//            fileDescriptor.startOffset,
//            fileDescriptor.declaredLength
//        )
//    }
//
//    // 🔥 UPDATED PREDICT FUNCTION
//    fun predict(bitmap: Bitmap): Pair<String, Float> {
//
//        val inputBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
//        val byteBuffer = convertBitmapToByteBuffer(inputBitmap)
//
//        val output = Array(1) { FloatArray(labels.size) }
//        interpreter.run(byteBuffer, output)
//
//        // 🔥 APPLY SOFTMAX (IMPORTANT FIX)
//        val rawOutput = output[0]
//        val confidences = softmax(rawOutput)
//
//        val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1
//        val confidence = if (maxIndex != -1) confidences[maxIndex] else 0f
//        val breedName = if (maxIndex != -1) labels[maxIndex] else "Unknown"
//
//        return breedName to confidence
//    }
//
//    // 🔥 STEP 2 FIX (NORMALIZATION)
//    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
//
//        val byteBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4)
//        byteBuffer.order(ByteOrder.nativeOrder())
//
//        val intValues = IntArray(224 * 224)
//        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
//
//        for (pixel in intValues) {
//
//            val r = ((pixel shr 16) and 0xFF)
//            val g = ((pixel shr 8) and 0xFF)
//            val b = (pixel and 0xFF)
//
//            // 🔥 CRITICAL FIX
//            byteBuffer.putFloat((r - 127.5f) / 127.5f)
//            byteBuffer.putFloat((g - 127.5f) / 127.5f)
//            byteBuffer.putFloat((b - 127.5f) / 127.5f)
//        }
//
//        byteBuffer.rewind()
//        return byteBuffer
//    }
//
//    // 🔥 SOFTMAX FUNCTION (NEW)
//    private fun softmax(logits: FloatArray): FloatArray {
//        val expValues = logits.map { exp(it.toDouble()) }.toDoubleArray()
//        val sumExp = expValues.sum()
//
//        return FloatArray(logits.size) { i ->
//            (expValues[i] / sumExp).toFloat()
//        }
//    }
//}
