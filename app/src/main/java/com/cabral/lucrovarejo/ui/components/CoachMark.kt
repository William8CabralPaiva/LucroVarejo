package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** Elementos da tela da maquininha que o tour pode destacar. */
enum class CoachTarget {
    NAME_FIELD,
    CREDIT_TAB,
    ENABLE_SWITCH,
    BRAND_FIELD,
    BRAND_CHIP,
    ADD_INSTALLMENT,
    INSTALLMENT_RATE,
    SAVE,
}

/** Guarda a posição (na janela) e o requester de rolagem de cada alvo composto. */
@Stable
class CoachMarkState {
    internal val bounds = mutableStateMapOf<CoachTarget, Rect>()
    internal val requesters = mutableMapOf<CoachTarget, BringIntoViewRequester>()
}

@Composable
fun rememberCoachMarkState(): CoachMarkState = remember { CoachMarkState() }

/** Registra o composable como alvo do coachmark. Sem [state], não altera o modifier. */
fun Modifier.coachTarget(state: CoachMarkState?, target: CoachTarget): Modifier {
    if (state == null) return this
    return composed {
        val requester = remember { BringIntoViewRequester() }
        DisposableEffect(state, target) {
            state.requesters[target] = requester
            onDispose {
                state.requesters.remove(target)
                state.bounds.remove(target)
            }
        }
        bringIntoViewRequester(requester).onGloballyPositioned {
            state.bounds[target] = it.boundsInWindow()
        }
    }
}

data class CoachStep(
    val target: CoachTarget,
    val titleResId: Int,
    val textResId: Int,
    val position: Int,
    val total: Int,
    val nextLabelResId: Int? = null,
    val nextEnabled: Boolean = true,
)

/**
 * Escurece a tela deixando um "buraco" sobre o alvo. O buraco não é interceptado, então o
 * toque chega ao elemento real; o restante da tela fica bloqueado até o usuário agir.
 */
@Composable
fun CoachMarkOverlay(
    state: CoachMarkState,
    step: CoachStep,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(step.target) {
        delay(SCROLL_DELAY_MS)
        state.requesters[step.target]?.bringIntoView()
    }

    var origin by remember { mutableStateOf(Offset0) }
    var size by remember { mutableStateOf(IntSize.Zero) }
    val padding = with(LocalDensity.current) { dimensionResource(R.dimen.coach_mark_hole_padding).toPx() }
    val hole = state.bounds[step.target]?.translate(-origin.x, -origin.y)?.inflate(padding)
    val scrim = Color.Black.copy(alpha = SCRIM_ALPHA)

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned {
                val bounds = it.boundsInWindow()
                origin = Offset(bounds.left, bounds.top)
                size = it.size
            }
    ) {
        if (hole == null) {
            Scrim(0f, 0f, size.width.toFloat(), size.height.toFloat(), scrim)
        } else {
            Scrim(0f, 0f, size.width.toFloat(), hole.top, scrim)
            Scrim(0f, hole.bottom, size.width.toFloat(), size.height - hole.bottom, scrim)
            Scrim(0f, hole.top, hole.left, hole.height, scrim)
            Scrim(hole.right, hole.top, size.width - hole.right, hole.height, scrim)
            Box(
                modifier = Modifier
                    .offset { IntOffset(hole.left.roundToInt(), hole.top.roundToInt()) }
                    .size(
                        width = with(LocalDensity.current) { hole.width.toDp() },
                        height = with(LocalDensity.current) { hole.height.toDp() }
                    )
                    .border(
                        dimensionResource(R.dimen.coach_mark_hole_border),
                        Color.White,
                        RoundedCornerShape(dimensionResource(R.dimen.coach_mark_radius))
                    )
            )
        }

        val targetInLowerHalf = hole != null && hole.center.y > size.height / 2f
        CoachMarkBubble(
            step = step,
            onNext = onNext,
            onSkip = onSkip,
            modifier = Modifier
                .align(if (targetInLowerHalf) Alignment.TopCenter else Alignment.BottomCenter)
                .padding(dimensionResource(R.dimen.coach_mark_padding))
        )
    }
}

@Composable
private fun Scrim(left: Float, top: Float, width: Float, height: Float, color: Color) {
    if (width <= 0f || height <= 0f) return
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
            .size(with(density) { width.toDp() }, with(density) { height.toDp() })
            .background(color)
            .pointerInput(Unit) { detectTapGestures { } }
    )
}

@Composable
private fun CoachMarkBubble(
    step: CoachStep,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.coach_mark_radius)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.coach_mark_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.coach_mark_spacing))
        ) {
            Text(
                text = stringResource(R.string.coach_mark_step_counter, step.position, step.total),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(step.titleResId),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = stringResource(step.textResId), style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onSkip) { Text(stringResource(R.string.coach_mark_skip)) }
                step.nextLabelResId?.let {
                    TextButton(onClick = onNext, enabled = step.nextEnabled) { Text(stringResource(it)) }
                }
            }
        }
    }
}

private val Offset0 = Offset.Zero
private const val SCRIM_ALPHA = 0.7f
private const val SCROLL_DELAY_MS = 150L

@Preview(showBackground = true, name = "CoachMark - sem alvo")
@Composable
private fun CoachMarkOverlayPreview() {
    LucroVarejoTheme {
        CoachMarkOverlay(
            state = rememberCoachMarkState(),
            step = CoachStep(
                target = CoachTarget.NAME_FIELD,
                titleResId = R.string.coach_tour_name_title,
                textResId = R.string.coach_tour_name_text,
                position = 1,
                total = 3,
                nextLabelResId = R.string.coach_mark_next
            ),
            onNext = {},
            onSkip = {}
        )
    }
}
