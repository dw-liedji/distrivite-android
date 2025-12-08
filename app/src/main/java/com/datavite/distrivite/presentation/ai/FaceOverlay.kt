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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.datavite.distrivite.presentation.ai.model.FaceFrame

@Composable
fun FaceOverLay(
    faceFrame: FaceFrame?,
){
    faceFrame?.let {
        val bitmapWidth = it.bitmap.width.toFloat()
        val bitmapHeight = it.bitmap.height.toFloat()
        val bitmapRotation = it.rotation

        for (face in it.faces){
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