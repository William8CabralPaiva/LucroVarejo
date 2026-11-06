
package com.cabral.lucrovarejo.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val COIN_COUNT = 550

/** Duração total da cortina de moedas. */
const val COIN_TRANSITION_MS = 1_400

/** Instante em que a tela está coberta e deve ser trocada. */
const val COIN_SWAP_MS = COIN_TRANSITION_MS / 2

private const val MAX_START = 0.5f

private val CoinGreen = Color(0xFF2E9E5B)
private val CoinEdge = Color(0xFF14532D)
private val CoinShine = Color(0xFF86EFAC)

private class Coin(
    val x: Float,
    val start: Float,
    val radiusDp: Float,
    val spin: Float,
    val phase: Float,
    val sway: Float
)

@Composable
fun CoinTransition(
    trigger: Any?,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(1f) }

    var seed by remember { mutableStateOf(0) }
    var first by remember { mutableStateOf(true) }

    LaunchedEffect(trigger) {
        if (first) {
            first = false
            return@LaunchedEffect
        }

        if (!enabled) return@LaunchedEffect

        seed++
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = COIN_TRANSITION_MS,
                easing = LinearEasing
            )
        )
    }

    if (progress.value >= 1f) return

    val coins = remember(seed) {
        val random = Random(seed)

        List(COIN_COUNT) {
            Coin(
                x = random.nextFloat(),
                start = random.nextFloat() * MAX_START,
                radiusDp = 28f + random.nextFloat() * 18f,
                spin = 1.5f + random.nextFloat() * 3f,
                phase = random.nextFloat() * 2f * PI.toFloat(),
                sway = 0.01f + random.nextFloat() * 0.03f
            )
        }
    }

    val dollarPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.BLACK
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val fall = 1f - MAX_START

        coins.forEach { coin ->
            val local = ((progress.value - coin.start) / fall)
                .coerceIn(0f, 1f)

            if (progress.value < coin.start || local >= 1f) {
                return@forEach
            }

            val radius = coin.radiusDp * density

            val centerX = (
                    coin.x + coin.sway * sin(coin.phase + local * 6f)
                    ) * size.width

            val centerY = -radius + local * (size.height + radius * 2)

            val width = abs(
                cos(local * coin.spin * PI.toFloat() + coin.phase)
            ).coerceAtLeast(0.3f) * radius

            val topLeft = Offset(
                centerX - width,
                centerY - radius
            )

            val coinSize = Size(
                width * 2,
                radius * 2
            )

            // Corpo da moeda
            drawOval(
                color = CoinGreen,
                topLeft = topLeft,
                size = coinSize
            )

            // Contorno
            drawOval(
                color = CoinEdge,
                topLeft = topLeft,
                size = coinSize,
                style = Stroke(radius * 0.16f)
            )

            // Brilho
            drawOval(
                color = CoinEdge,
                topLeft = Offset(
                    centerX - width * 0.55f,
                    centerY - radius * 0.55f
                ),
                size = Size(
                    width * 1.1f,
                    radius * 1.1f
                ),
                alpha = 0.7f,
                style = Stroke(radius * 0.1f)
            )

            // Símbolo de dinheiro
            dollarPaint.textSize = radius * 1.1f

            drawContext.canvas.nativeCanvas.drawText(
                "$",
                centerX,
                centerY + radius * 0.35f,
                dollarPaint
            )
        }
    }
}
