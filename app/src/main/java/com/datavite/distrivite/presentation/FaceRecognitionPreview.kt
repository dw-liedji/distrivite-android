package com.datavite.distrivite.presentation

import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.datavite.distrivite.presentation.recognition.CameraPreview
import com.datavite.distrivite.domain.model.DomainFaceRecognition

@Composable
fun FaceRecognitionPreview(viewModel: FaceRecognitionViewModel) {
    val faceRecognitionResults by viewModel.faceRecognitionResults.observeAsState(emptyList())

    Box(
        modifier = Modifier.fillMaxSize() ,
    ) {
        CameraPreview(viewModel.getFaceAnalyzer(), Modifier.fillMaxSize())
        OverlayFaceRecognitionResults(faceRecognitionResults)
    }
}

@Composable
fun OverlayFaceRecognitionResults(faceRecognitions: List<DomainFaceRecognition>) {
    Box(modifier = Modifier.fillMaxSize().aspectRatio(1.0f)) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1.0f)) {
            faceRecognitions.forEach { faceRecognition ->
                val face = faceRecognition.face
                val bounds = face.boundingBox

                // Draw bounding box
                drawBoundingBox(bounds)

                // Draw landmarks
                //drawLandmarks(face)

                // Draw contours
                //drawContours(face)
            }
        }
    }
}

private fun DrawScope.drawBoundingBox(bounds: Rect) {
    drawRect(
        color = Color.Red,
        topLeft = androidx.compose.ui.geometry.Offset(
            bounds.left.toFloat(),
            bounds.top.toFloat()
        ),
        size = androidx.compose.ui.geometry.Size(
            bounds.width().toFloat(),
            bounds.height().toFloat()
        ),
        style = Stroke(width = 2.dp.toPx())
    )
}

private fun DrawScope.drawLandmarks(face: com.google.mlkit.vision.face.Face) {
    face.allLandmarks.forEach { landmark ->
        drawCircle(
            color = Color.Green,
            radius = 4.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(
                landmark.position.x,
                landmark.position.y
            )
        )
    }
}

private fun DrawScope.drawContours(face: com.google.mlkit.vision.face.Face) {
    face.allContours.forEach { contour ->
        contour.points.forEach { point ->
            drawCircle(
                color = Color.Blue,
                radius = 2.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(
                    point.x,
                    point.y
                )
            )
        }
    }
}