package com.datavite.distrivite.presentation.welcome

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
@Composable
fun TiqTaqWatch(
    modifier: Modifier = Modifier,
) {
    var isTiq by remember { mutableStateOf(true) }
    val rotationAnimation = rememberInfiniteTransition(label = "RotationAnimation")
    val rotation by rotationAnimation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "RotationAngle"
    )

    LaunchedEffect(Unit) {
        while (true) {
            isTiq = !isTiq
            delay(1000) // 1-second interval
        }
    }

    // Resolve colors in the composable context
    val circleColor = MaterialTheme.colorScheme.surface
    val arcColor = if (isTiq) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Rotating watch face
        Canvas(
            modifier = Modifier.size(200.dp)
        ) {
            drawCircle(
                color = circleColor, // Resolved outside DrawScope
                radius = size.minDimension / 2,
                style = Stroke(width = 12.dp.toPx())
            )

            drawArc(
                color = arcColor, // Resolved outside DrawScope
                startAngle = 0f,
                sweepAngle = rotation,
                useCenter = false,
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // "Tiq" or "Taq" Text
        Text(
            text = if (isTiq) "Tiq" else "Taq",
            style = MaterialTheme.typography.displayLarge.copy(color = arcColor),
            modifier = Modifier.align(Alignment.Center)
        )

        // "Powered by Datavite" Text
        Text(
            text = "Powered by Datavite",
            style = MaterialTheme.typography.bodySmall.copy(color = textColor),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
