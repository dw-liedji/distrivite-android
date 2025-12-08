package com.datavite.distrivite.presentation.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import kotlin.math.abs

class FaceAntiSpoofingPlainDetector(context: Context) {

    companion object {
        private const val MODEL_FILE = "FaceAntiSpoofing.tflite"

        const val INPUT_IMAGE_SIZE = 256 // Required placeholder image width/height
        const val THRESHOLD = 0.2f // Threshold for determining spoofing
        const val ROUTE_INDEX = 6 // Observed route index during training

        /**
         * Normalize image to [0, 1]
         */
        fun normalizeImage(bitmap: Bitmap): Array<Array<FloatArray>> {
            val height = bitmap.height
            val width = bitmap.width
            val floatValues = Array(height) { Array(width) { FloatArray(3) } }

            val imageStd = 255.0f
            val pixels = IntArray(height * width)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

            for (i in 0 until height) {
                for (j in 0 until width) {
                    val pixel = pixels[i * width + j]
                    val r = ((pixel shr 16) and 0xFF) / imageStd
                    val g = ((pixel shr 8) and 0xFF) / imageStd
                    val b = (pixel and 0xFF) / imageStd
                    floatValues[i][j] = floatArrayOf(r, g, b)
                }
            }
            return floatValues
        }
    }

    private lateinit var interpreter: Interpreter

    init {
        val options = Interpreter.Options().apply {
            //Interpreter.Options.setNumThreads = 4
        }
        try {
            interpreter = Interpreter(
                FileUtil.loadMappedFile(context, MODEL_FILE) , options )
        }catch (e:Exception) {e.printStackTrace() }
    }

    /**
     * Anti-spoofing check
     * @param bitmap
     * @return Score
     */
    fun antiSpoofing(bitmap: Bitmap): Float {
        val bitmapScaled = Bitmap.createScaledBitmap(bitmap, INPUT_IMAGE_SIZE, INPUT_IMAGE_SIZE, true)
        val img = normalizeImage(bitmapScaled)
        val input = arrayOf(img)
        val clssPred = Array(1) { FloatArray(8) }
        val leafNodeMask = Array(1) { FloatArray(8) }
        val outputs = mapOf(
            interpreter.getOutputIndex("Identity") to clssPred,
            interpreter.getOutputIndex("Identity_1") to leafNodeMask
        )

        interpreter.runForMultipleInputsOutputs(arrayOf(input), outputs)

        Log.i("FaceAntiSpoofing", clssPred[0].contentToString())
        Log.i("FaceAntiSpoofing", leafNodeMask[0].contentToString())

        return leafScore1(clssPred, leafNodeMask)
    }

    private fun leafScore1(clssPred: Array<FloatArray>, leafNodeMask: Array<FloatArray>): Float {
        var score = 0f
        for (i in 0 until 8) {
            score += abs(clssPred[0][i]) * leafNodeMask[0][i]
        }
        return score
    }

    private fun leafScore2(clssPred: Array<FloatArray>): Float {
        return clssPred[0][ROUTE_INDEX]
    }

    fun isLiveFace(bitmap: Bitmap) : Boolean {
        val spoofDetectionScore = antiSpoofing(bitmap)
        return spoofDetectionScore < THRESHOLD
    }

}
