import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.datavite.distrivite.data.local.datasource.ai.FaceRecognitionDataSource

class FaceRecognitionAnalyzer(private val dataSource: FaceRecognitionDataSource) : ImageAnalysis.Analyzer {



    private var frameSkipCounter = 0

    override fun analyze(image: ImageProxy) {
        // Process every 60th frame or adjust based on performance needs
        if (frameSkipCounter % 60 == 0) {
                val rotationDegrees = image.imageInfo.rotationDegrees
                val bitmap = image.toBitmap() // Ensure this method is efficient
                try {
                    dataSource.processFaceRecognitions(bitmap, rotationDegrees)
                } catch (e: Exception) {
                    // Handle exceptions and log errors
                    Log.e("FaceRecognitionAnalyzer", "Face recognition failed", e)
                }

        }
        frameSkipCounter++

        image.close() // Ensure image is closed to avoid memory leaks
    }

    fun getDatasource() = dataSource
}
