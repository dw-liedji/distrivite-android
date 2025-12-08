package com.datavite.distrivite.presentation.ai

import android.graphics.Typeface
import android.text.TextPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datavite.distrivite.presentation.ai.model.FaceFrameRecognitionSecureResult

@Composable
fun FaceOverLayResult(
    faceFrameRecognitionSecureResult: FaceFrameRecognitionSecureResult? = null,
    showFailureCrossMark:Boolean = false, showSuccessCheckMark:Boolean = true
){
    faceFrameRecognitionSecureResult?.let {
    val bitmapWidth = faceFrameRecognitionSecureResult.faceFrame.bitmap.width.toFloat()
        val bitmapHeight = faceFrameRecognitionSecureResult.faceFrame.bitmap.height.toFloat()
        val bitmapRotation = faceFrameRecognitionSecureResult.faceFrame.rotation

        for (face in faceFrameRecognitionSecureResult.faceFrame.faces){
            face.let {
                Canvas(modifier = Modifier.fillMaxSize()) {

                    if (bitmapWidth> 0 && bitmapHeight > 0) {
                        // Calculate the width and height ratios
                        val widthRatio = size.width / bitmapWidth
                        val heightRatio = size.height / bitmapHeight

                        // Apply rotation and translate transformation
                        withTransform({
                            rotate(
                                degrees = (bitmapRotation  + 90 ).toFloat(),
                                pivot = Offset(size.width / 2, size.height / 2)
                            )
                        }) {

                            val rectTopLeft = Offset(
                                it.boundingBox.left * widthRatio,
                                it.boundingBox.top * heightRatio
                            )

                            val rectSize = Size(
                                it.boundingBox.width() * widthRatio,
                                it.boundingBox.height() * heightRatio
                            )


                            drawRect(
                                color = Color.Red,
                                topLeft = rectTopLeft,
                                size = rectSize,
                                style = Stroke(width = 3.dp.toPx())
                            )
                            if(showSuccessCheckMark) {
                                // Calculate the size and position of the check mark
                                val checkStartX = rectTopLeft.x + rectSize.width * 0.2f
                                val checkStartY = rectTopLeft.y + rectSize.height * 0.5f
                                val checkMidX = rectTopLeft.x + rectSize.width * 0.4f
                                val checkMidY = rectTopLeft.y + rectSize.height * 0.7f
                                val checkEndX = rectTopLeft.x + rectSize.width * 0.8f
                                val checkEndY = rectTopLeft.y + rectSize.height * 0.3f

                                // Draw the success checkmark using a path
                                val path = Path().apply {
                                    moveTo(checkStartX, checkStartY)
                                    lineTo(checkMidX, checkMidY)
                                    lineTo(checkEndX, checkEndY)
                                }

                                drawPath(
                                    path = path,
                                    color = Color.Green,
                                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            /*
                            // draw failure mark
                            // Calculate the cross lines start and end points
                            val startOffset1 = Offset(rectTopLeft.x, rectTopLeft.y)
                            val endOffset1 = Offset(rectTopLeft.x + rectSize.width, rectTopLeft.y + rectSize.height)

                            val startOffset2 = Offset(rectTopLeft.x + rectSize.width, rectTopLeft.y)
                            val endOffset2 = Offset(rectTopLeft.x, rectTopLeft.y + rectSize.height)

                            // Draw the failure cross mark using two intersecting lines
                            drawLine(
                                color = Color.Red,
                                start = startOffset1,
                                end = endOffset1,
                                strokeWidth = 6.dp.toPx(),
                                cap = StrokeCap.Round
                            )

                            drawLine(
                                color = Color.Red,
                                start = startOffset2,
                                end = endOffset2,
                                strokeWidth = 6.dp.toPx(),
                                cap = StrokeCap.Round
                            ) */

                            if (showFailureCrossMark) {
                                // Calculate padding in pixels
                                val paddingPx = 16.dp.toPx()

                                // Calculate the cross lines start and end points with padding
                                val startOffset1 = Offset(rectTopLeft.x + paddingPx, rectTopLeft.y + paddingPx)
                                val endOffset1 = Offset(rectTopLeft.x + rectSize.width - paddingPx, rectTopLeft.y + rectSize.height - paddingPx)

                                val startOffset2 = Offset(rectTopLeft.x + rectSize.width - paddingPx, rectTopLeft.y + paddingPx)
                                val endOffset2 = Offset(rectTopLeft.x + paddingPx, rectTopLeft.y + rectSize.height - paddingPx)

                                // Draw the failure cross mark using two intersecting lines with padding
                                drawLine(
                                    color = Color.Red,
                                    start = startOffset1,
                                    end = endOffset1,
                                    strokeWidth = 6.dp.toPx(),
                                    cap = StrokeCap.Round
                                )

                                drawLine(
                                    color = Color.Red,
                                    start = startOffset2,
                                    end = endOffset2,
                                    strokeWidth = 6.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }

                            // Draw contours
                            it.allContours.let { it ->
                                for (contour in it) {
                                    for (point in contour.points) {
                                        // Draw each point in the contour
                                        drawCircle(
                                            color = Color.Green,
                                            radius = 3.dp.toPx(),
                                            center = Offset(
                                                point.x * widthRatio,
                                                point.y * heightRatio
                                            )
                                        )
                                    }
                                }
                            }

                            for (result in faceFrameRecognitionSecureResult.faceRecognitionSecureResults) {
                                if (result.face == it){
                                    // Draw faceRecognitionSecureResult if available
                                    val username = result.faceRecognitionResult.name
                                    val isSecure = result.isSecure
                                    val confidence = result.faceRecognitionResult.confidence
                                    val recognitionText = "$username: ${String.format("%.2f", confidence * 100)}% Live:${isSecure}"

                                    val faceRecognitionSecureResultTextPaint = TextPaint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 16.sp.toPx()
                                        //Paint.setTypeface = Typeface.DEFAULT_BOLD
                                    }

                                    drawIntoCanvas { canvas ->
                                        // Draw recognition text just above the rectangle
                                        canvas.nativeCanvas.drawText(
                                            recognitionText,
                                            rectTopLeft.x,
                                            rectTopLeft.y - 5.dp.toPx(), // Adjust position above the rectangle
                                            faceRecognitionSecureResultTextPaint
                                        )
                                    }
                                }
                            }

                            // Draw classifications
                            val offsetX = it.boundingBox.right * widthRatio + 5.dp.toPx()
                            var offsetY = it.boundingBox.top * heightRatio - 5.dp.toPx()

                            // Define text style and paint
                            val textPaint = TextPaint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = 16.sp.toPx()
                                typeface = Typeface.DEFAULT
                            }

                            // Draw text for smile probability
                            it.smilingProbability?.let { probability ->
                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawText(
                                        if (probability > 0.5f) "Smiling" else "Not Smiling",
                                        offsetX,
                                        offsetY,
                                        textPaint
                                    )
                                }
                                offsetY += 20.dp.toPx()
                            }

                            // Draw text for left eye open probability
                            it.leftEyeOpenProbability?.let { probability ->
                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawText(
                                        if (probability > 0.5f) "Left Eye Open" else "Left Eye Closed",
                                        offsetX,
                                        offsetY,
                                        textPaint
                                    )
                                }
                                offsetY += 20.dp.toPx()
                            }

                            // Draw text for right eye open probability
                            it.rightEyeOpenProbability?.let { probability ->
                                drawIntoCanvas { canvas ->
                                    canvas.nativeCanvas.drawText(
                                        if (probability > 0.5f) "Right Eye Open" else "Right Eye Closed",
                                        offsetX,
                                        offsetY,
                                        textPaint
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}