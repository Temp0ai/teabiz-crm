package com.teabiz.crm.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    // Tea pour animation - one-shot using Animatable
    val pourProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        pourProgress.animateTo(1f, animationSpec = tween(1200, delayMillis = 300, easing = FastOutSlowInEasing))
    }

    // Steam opacity - infinite loop
    val steamAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "steam"
    )

    // Steam float up - infinite loop
    val steamOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -30f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "steamOffset"
    )

    // Brand text fade in - one-shot
    val textAlpha = remember { Animatable(0f) }
    val textOffset = remember { Animatable(15f) }
    LaunchedEffect(Unit) {
        delay(800)
        textAlpha.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        delay(800)
        textOffset.animateTo(0f, animationSpec = tween(800, easing = FastOutSlowInEasing))
    }

    LaunchedEffect(Unit) {
        delay(1500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f

            // Soft floor shadow
            drawOval(
                color = Color(0x10000000),
                topLeft = Offset(cx - 60.dp.toPx(), h * 0.68f),
                size = Size(120.dp.toPx(), 16.dp.toPx())
            )

            // Tea stream
            val pour = pourProgress.value
            if (pour < 0.8f) {
                val streamAlpha = if (pour < 0.7f) 1f else (1f - (pour - 0.7f) / 0.1f)
                drawLine(
                    color = Color(0xFFD35400).copy(alpha = streamAlpha.coerceIn(0f, 1f)),
                    start = Offset(cx, 0f),
                    end = Offset(cx, h * 0.56f),
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Cup body
            val cupLeft = cx - 50.dp.toPx()
            val cupRight = cx + 50.dp.toPx()
            val cupTop = h * 0.52f
            val cupBottom = h * 0.68f
            val cupRadius = 12.dp.toPx()

            // Cup shadow
            drawRoundRect(
                color = Color(0x15000000),
                topLeft = Offset(cupLeft + 4.dp.toPx(), cupTop + 8.dp.toPx()),
                size = Size(cupRight - cupLeft, cupBottom - cupTop),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cupRadius)
            )

            // Cup body
            drawRoundRect(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFEAE6DF)),
                    start = Offset(cupLeft, cupTop),
                    end = Offset(cupRight, cupBottom)
                ),
                topLeft = Offset(cupLeft, cupTop),
                size = Size(cupRight - cupLeft, cupBottom - cupTop),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cupRadius)
            )

            // Cup handle
            drawArc(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(Color(0xFFFFFFFF), Color(0xFFEAE6DF)),
                    start = Offset(cupRight, cupTop),
                    end = Offset(cupRight + 25.dp.toPx(), cupBottom)
                ),
                startAngle = -90f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(cupRight - 2.dp.toPx(), cupTop + 10.dp.toPx()),
                size = Size(25.dp.toPx(), cupBottom - cupTop - 20.dp.toPx()),
                style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
            )

            // Steam lines
            val steamPaths = listOf(
                cx - 15.dp.toPx(),
                cx,
                cx + 15.dp.toPx()
            )
            steamPaths.forEachIndexed { index, sx ->
                val yOffset = steamOffset + (index * 5.dp.toPx())
                val alpha = steamAlpha * (1f - index * 0.15f)
                drawLine(
                    color = Color.White.copy(alpha = alpha.coerceIn(0f, 0.6f)),
                    start = Offset(sx, cupTop - 5.dp.toPx() + yOffset),
                    end = Offset(sx + (index - 1) * 3.dp.toPx(), cupTop - 35.dp.toPx() + yOffset),
                    strokeWidth = (6 - index * 2).dp.toPx().coerceAtLeast(2.dp.toPx()),
                    cap = StrokeCap.Round
                )
            }
        }

        // Brand text at bottom
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = textOffset.value.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Arihant's Natural",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0A6B3B).copy(alpha = textAlpha.value)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Let's Brew",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFED1C24).copy(alpha = textAlpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "LOADING CRM",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFA0A0A0).copy(alpha = textAlpha.value),
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
