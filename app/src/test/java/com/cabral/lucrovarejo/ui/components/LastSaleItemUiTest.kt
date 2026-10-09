package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LastSaleItemUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun displaysProductPriceAndTodayLabel() {
        val saleDate = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 32)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        val saleTimestamp = Timestamp(saleDate)

        composeRule.setContent {
            LucroVarejoTheme {
                LastSaleItem(
                    productName = "Bolsa Feminina",
                    priceInCents = 8990,
                    timestamp = saleTimestamp,
                    onClick = {}
                )
            }
        }

        composeRule.onNodeWithText("Bolsa Feminina").assertIsDisplayed()
        composeRule.onNodeWithText("R$ 89,90").assertIsDisplayed()
        composeRule.onNodeWithText("Hoje").assertIsDisplayed()
    }

    @Test
    fun formatsYesterdayAndOlderDatesInBrazilianFormat() {
        val timeZone = TimeZone.getDefault()
        val now = localDate(2026, Calendar.OCTOBER, 9, 15, 0)
        val yesterdayTimestamp = Timestamp(localDate(2026, Calendar.OCTOBER, 8, 11, 20))
        val olderTimestamp = Timestamp(localDate(2026, Calendar.OCTOBER, 5, 16, 45))

        assertEquals(
            "Ontem",
            formatLastSaleTimestamp(yesterdayTimestamp, "Hoje", "Ontem", now, timeZone)
        )
        assertEquals(
            "05/10",
            formatLastSaleTimestamp(olderTimestamp, "Hoje", "Ontem", now, timeZone)
        )
    }

    private fun localDate(year: Int, month: Int, day: Int, hour: Int, minute: Int): Date =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day, hour, minute)
        }.time
}
