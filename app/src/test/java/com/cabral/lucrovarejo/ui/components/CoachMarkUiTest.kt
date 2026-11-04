package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CoachMarkUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun step(nextEnabled: Boolean = true, withNext: Boolean = true) = CoachStep(
        target = CoachTarget.NAME_FIELD,
        titleResId = R.string.coach_tour_credit_tab_title,
        textResId = R.string.coach_tour_credit_tab_text,
        position = 2,
        total = 5,
        nextLabelResId = if (withNext) R.string.coach_mark_next else null,
        nextEnabled = nextEnabled
    )

    @Test
    fun overlayShowsStepCounterTitleAndActions() {
        var next = 0
        var skip = 0
        composeRule.setContent {
            LucroVarejoTheme {
                CoachMarkOverlay(
                    state = rememberCoachMarkState(),
                    step = step(),
                    onNext = { next++ },
                    onSkip = { skip++ }
                )
            }
        }

        composeRule.onNodeWithText("Passo 2 de 5").assertIsDisplayed()
        composeRule.onNodeWithText("Abra a aba Crédito").assertIsDisplayed()
        composeRule.onNodeWithText("Próximo").performClick()
        composeRule.onNodeWithText("Pular").performClick()
        composeRule.runOnIdle {
            assertEquals(1, next)
            assertEquals(1, skip)
        }
    }

    @Test
    fun nextButtonIsDisabledWhenStepIsNotReady() {
        composeRule.setContent {
            LucroVarejoTheme {
                CoachMarkOverlay(
                    state = rememberCoachMarkState(),
                    step = step(nextEnabled = false),
                    onNext = {},
                    onSkip = {}
                )
            }
        }

        composeRule.onNodeWithText("Próximo").assertIsNotEnabled()
    }

    @Test
    fun nextButtonIsHiddenWhenStepHasNoLabel() {
        composeRule.setContent {
            LucroVarejoTheme {
                CoachMarkOverlay(
                    state = rememberCoachMarkState(),
                    step = step(withNext = false),
                    onNext = {},
                    onSkip = {}
                )
            }
        }

        composeRule.onNodeWithText("Próximo").assertDoesNotExist()
        composeRule.onNodeWithText("Pular").assertIsDisplayed()
    }
}
