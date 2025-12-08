package com.datavite.distrivite.presentation.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.datavite.distrivite.presentation.ai.model.ModelInfo
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.TensorOperator
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat
import java.nio.ByteBuffer
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

class FaceEmbeddingProcessor @Inject constructor   (
    context : Context,
    useGpu : Boolean,
    useXNNPack : Boolean) {

    companion object {

        val FACENET = ModelInfo(
            "FaceNet",
            "facenet.tflite",
            0.4f,
            10f,
            128,
            160
        )

        val FACENET_512 = ModelInfo(
            "FaceNet-512",
            "facenet_512.tflite",
            0.3f,
            23.56f,
            512,
            160
        )

        val FACENET_QUANTIZED = ModelInfo(
            "FaceNet Quantized",
            "facenet_int_quantized.tflite",
            0.4f,
            10f,
            128,
            160
        )

        val FACENET_512_QUANTIZED = ModelInfo(
            "FaceNet-512 Quantized",
            "facenet_512_int_quantized.tflite",
            0.3f,
            23.56f,
            512,
            160
        )

    }

    private val model = FACENET
    // Input image size for FaceNet model.
    private val imgSize = model.inputDims

    // Output embedding size
    private val embeddingDim = model.outputDims

    private var interpreter : Interpreter
    private val imageTensorProcessor = ImageProcessor.Builder()
        .add( ResizeOp( imgSize , imgSize , ResizeOp.ResizeMethod.BILINEAR ) )
        .add( StandardizeOp() )
        .build()

    init {
        // Initialize TFLiteInterpreter
        val interpreterOptions = Interpreter.Options().apply {
            // Add the GPU Delegate if supported.
            // See -> https://www.tensorflow.org/lite/performance/gpu#android
            if ( useGpu ) {
                if ( CompatibilityList().isDelegateSupportedOnThisDevice ) {
                    addDelegate( GpuDelegate( CompatibilityList().bestOptionsForThisDevice ))
                }
            }
            else {
                // Number of threads for computation
                setNumThreads(2)
            }
            setUseXNNPACK(useXNNPack)
            setUseNNAPI(true)
        }
        interpreter = Interpreter(FileUtil.loadMappedFile(context, model.assetsFilename ) , interpreterOptions )
        Log.i( "distrivite-ai","Using ${model.name} model.")
    }


    // Gets an face embedding using FaceNet.
    fun process( image : Bitmap) : FloatArray {
        return runFaceNet( convertBitmapToBuffer( image ))[0]
    }


    // Run the FaceNet model.
    private fun runFaceNet(inputs: Any): Array<FloatArray> {
        val t1 = System.currentTimeMillis()
        val faceNetModelOutputs = Array( 1 ){ FloatArray( embeddingDim ) }
        interpreter.run( inputs, faceNetModelOutputs )
        Log.i( "Performance" , "${model.name} Inference Speed in ms : ${System.currentTimeMillis() - t1}")
        return faceNetModelOutputs
    }


    // Resize the given bitmap and convert it to a ByteBuffer
    private fun convertBitmapToBuffer( image : Bitmap) : ByteBuffer {
        return imageTensorProcessor.process( TensorImage.fromBitmap( image ) ).buffer
    }


    // Op to perform standardization
    // x' = ( x - mean ) / std_dev
    class StandardizeOp : TensorOperator {

        override fun apply(p0: TensorBuffer?): TensorBuffer {
            val pixels = p0!!.floatArray
            val mean = pixels.average().toFloat()
            var std = sqrt( pixels.map{ pi -> ( pi - mean ).pow( 2 ) }.sum() / pixels.size.toFloat() )
            std = max( std , 1f / sqrt( pixels.size.toFloat() ))
            for ( i in pixels.indices ) {
                pixels[ i ] = ( pixels[ i ] - mean ) / std
            }
            val output = TensorBufferFloat.createFixedSize( p0.shape , DataType.FLOAT32 )
            output.loadArray( pixels )
            return output
        }

    }

}
