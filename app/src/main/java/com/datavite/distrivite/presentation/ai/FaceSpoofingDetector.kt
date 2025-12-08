package com.datavite.distrivite.presentation.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

// Mask Detection model
// Source -> https://github.com/achen353/Face-Mask-Detector
class FaceSpoofingDetector @Inject constructor  (context: Context ) {
    companion object {
        const val THRESHOLD = 0.2f
        const val LAP_VARIANCE_THRESHOLD = 100.0f
        const val MODEL_PATH = "FaceAntiSpoofing.tflite"
        val imgSize = 256
        val numClasses = 2
        private const val SPOOF = "spoof"
        private const val NO_SPOOF = "no spoof"
        val classIndexToLabel = mapOf(
            0 to SPOOF ,
            1 to NO_SPOOF ,
        )
    }


    private lateinit var interpreter : Interpreter

    init {
        try {
            interpreter = Interpreter(FileUtil.loadMappedFile( context, MODEL_PATH ) , createInterpreterOptions() )
        }catch (e:Exception) {e.printStackTrace() }
    }

    private fun preprocessImage(bitmap: Bitmap): TensorImage {
        // Create a TensorImage object
        val tensorImage = TensorImage.fromBitmap(bitmap)
        // Create an ImageProcessor with desired operations
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(imgSize, imgSize, ResizeOp.ResizeMethod.BILINEAR)) // Resize to 224x224
            .add(NormalizeOp(0.0f, 255.0f)) // Normalize pixel values to [0, 1]
            .build()
        // Process the image
        return imageProcessor.process(tensorImage)
    }
    private fun createInterpreterOptions(): Interpreter.Options {
        val options = Interpreter.Options()
        val compatList = CompatibilityList()
        if (compatList.isDelegateSupportedOnThisDevice) {
            // Use GPU delegate if available
            val delegateOptions = compatList.bestOptionsForThisDevice
            val gpuDelegate = GpuDelegate(delegateOptions)
            options.addDelegate(gpuDelegate)
            Log.d("distrivite-ai", "GPU is available!")
        } else {
            // Use multi-threaded CPU if GPU is not supported
            val availableProcessors = Runtime.getRuntime().availableProcessors()
            options.setNumThreads(min(4, availableProcessors))
            Log.d("distrivite-ai", "GPU not available, using CPU with ${min(4, availableProcessors)} threads.")
        }
        options.setUseXNNPACK(true)
        return options
    }



     fun isLiveFace(bitmap: Bitmap): Boolean {
        val laplacianVariance = calculateLaplacianVariance(bitmap)
        if (laplacianVariance < LAP_VARIANCE_THRESHOLD) {
            Log.d("distrivite-ai", "Image is too blurry, possible spoofing detected.")
            return false
        }

        val score = score(bitmap)
        val isLive = score < THRESHOLD
        Log.d("distrivite-ai", if (isLive) "REAL $score" else "FAKE $score")
        return isLive
    }

    private fun score(bitmap: Bitmap): Float {
        val tensorImage = preprocessImage(bitmap)
        val input = tensorImage.buffer.rewind()

        val predictedClass = Array(1) { FloatArray(8) }

        val leafNodeMask = Array(1) { FloatArray(8) }

        val outputs = mapOf(
            interpreter.getOutputIndex("Identity") to predictedClass,
            interpreter.getOutputIndex("Identity_1") to leafNodeMask
        )

        val start = System.currentTimeMillis()
        interpreter.runForMultipleInputsOutputs(arrayOf(input), outputs)
        val end = System.currentTimeMillis()
        Log.d("distrivite-ai", "${end - start} ms")

        return calculateScoreByMin(predictedClass)
    }

    private fun calculateScoreByMin(predictedClass: Array<FloatArray>): Float {
        return predictedClass[0].minOrNull() ?: Float.MAX_VALUE
    }

    private fun calculateLaplacianVariance(bitmap: Bitmap): Float {
        val grayscaleBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val width = grayscaleBitmap.width
        val height = grayscaleBitmap.height
        val pixels = IntArray(width * height)
        grayscaleBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Convert to grayscale and compute Laplacian
        val grayscale = FloatArray(width * height)
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            grayscale[i] = 0.299f * r + 0.587f * g + 0.114f * b
        }

        val laplacian = FloatArray(width * height)
        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val index = y * width + x
                laplacian[index] = grayscale[index - 1] + grayscale[index + 1] +
                        grayscale[index - width] + grayscale[index + width] -
                        4 * grayscale[index]
            }
        }

        // Calculate variance of the Laplacian
        val mean = laplacian.average().toFloat()
        var variance = 0f
        for (value in laplacian) {
            variance += (value - mean).pow(2)
        }
        variance /= laplacian.size

        Log.d("distrivite-ai", "Laplacian variance: $variance")
        return variance
    }

    private fun calculateScoreByMax(predictedClass: Array<FloatArray>, leafNodeMask: Array<FloatArray>): Float {
        var score = 0f
        for (i in predictedClass[0].indices) {
            score += abs(predictedClass[0][i]) * leafNodeMask[0][i]
        }
        return score
    }


}