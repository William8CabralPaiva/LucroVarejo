package com.cabral.lucrovarejo.ui.screens.sales

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.util.Date
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SalesScreenUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun string(id: Int, vararg args: Any) =
        RuntimeEnvironment.getApplication().getString(id, *args)

    private fun sale(key: String, name: String, time: Long) = PurchaseModel(
        key = key,
        cod = "c",
        name = name,
        cost = 100,
        description = "",
        salePrice = 200,
        quantity = 1,
        saleDate = Date(time),
        paymentMethod = PaymentMethods.PIX
    )

    private fun show(
        state: SalesUiState,
        onQueryChange: (String) -> Unit = {},
        onDateSelected: (Long?) -> Unit = {},
        onLoadMore: () -> Unit = {},
        onRetry: () -> Unit = {},
        onAddPurchase: (() -> Unit)? = null,
        onEditPurchase: ((PurchaseModel) -> Unit)? = null
    ) = composeRule.setContent {
        LucroVarejoTheme {
            SalesContent(
                state = state,
                onQueryChange = onQueryChange,
                onDateSelected = onDateSelected,
                onLoadMore = onLoadMore,
                onRetry = onRetry,
                onAddPurchase = onAddPurchase,
                onEditPurchase = onEditPurchase
            )
        }
    }

    @Test
    fun buildSalesEntriesGroupsConsecutiveSalesByDay() {
        val utc = TimeZone.getTimeZone("UTC")
        val day = 24 * 60 * 60 * 1000L
        val sales = listOf(
            sale("1", "A", 10 * day), sale("2", "B", 10 * day + 1000), sale("3", "C", 12 * day)
        )

        val entries = buildSalesEntries(sales, utc)

        assertEquals(5, entries.size)
        assertTrue(entries[0] is SalesEntry.Header)
        assertEquals(SalesEntry.Sale(sales[1]), entries[2])
        assertTrue(entries[3] is SalesEntry.Header)
    }

    @Test
    fun showsErrorAndRetryWhenFirstLoadFails() {
        var retried = 0
        show(SalesUiState(isLoading = false, hasError = true), onRetry = { retried++ })

        composeRule.onNodeWithText(string(R.string.sales_load_error)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.sales_retry)).performClick()

        assertEquals(1, retried)
    }

    @Test
    fun listsSalesAndForwardsEditAndAddClicks() {
        var edited: PurchaseModel? = null
        var added = 0
        val item = sale("1", "Bolsa", 1_700_000_000_000)
        show(
            SalesUiState(items = listOf(item), isLoading = false),
            onAddPurchase = { added++ },
            onEditPurchase = { edited = it }
        )

        composeRule.onNodeWithText("Bolsa").performClick()
        composeRule.onNodeWithContentDescription(string(R.string.sales_add)).performClick()

        assertEquals(item, edited)
        assertEquals(1, added)
    }

    @Test
    fun showsRetryAfterLoadMoreFailureAndTriggersLoadMore() {
        var loadMore = 0
        var retried = 0
        show(
            SalesUiState(
                items = listOf(sale("1", "Bolsa", 1_700_000_000_000)),
                isLoading = false,
                hasError = true
            ),
            onLoadMore = { loadMore++ },
            onRetry = { retried++ }
        )

        composeRule.onNodeWithText(string(R.string.sales_retry)).performClick()

        assertEquals(1, retried)
        assertTrue(loadMore >= 1)
    }

    @Test
    fun showsLoadingMoreIndicatorWithItems() {
        show(
            SalesUiState(
                items = listOf(sale("1", "Bolsa", 1_700_000_000_000)),
                isLoading = false,
                isLoadingMore = true
            )
        )

        composeRule.onNodeWithText("Bolsa").assertIsDisplayed()
    }

    @Test
    fun searchFieldForwardsQuery() {
        var query = ""
        show(SalesUiState(isLoading = false), onQueryChange = { query = it })

        composeRule.onNodeWithText(string(R.string.sales_search_placeholder)).performTextInput("bol")

        assertEquals("bol", query)
    }

    @Test
    fun dateChipClearsFilterAndPickerAppliesSelection() {
        var selected: Long? = 1L
        show(
            SalesUiState(isLoading = false, selectedDateMillis = 1_759_708_800_000L),
            onDateSelected = { selected = it }
        )

        composeRule.onNodeWithContentDescription(string(R.string.sales_date_filter_clear))
            .performClick()
        assertEquals(null, selected)

        composeRule.onNodeWithContentDescription(string(R.string.sales_filter_by_date))
            .performClick()
        composeRule.onNodeWithText(string(R.string.date_picker_confirm)).performClick()
        assertEquals(1_759_708_800_000L, selected)
    }

    @Test
    fun datePickerCanBeDismissed() {
        show(SalesUiState(isLoading = false))

        composeRule.onNodeWithContentDescription(string(R.string.sales_filter_by_date))
            .performClick()
        composeRule.onNodeWithText(string(R.string.date_picker_cancel)).performClick()

        composeRule.onNodeWithContentDescription(string(R.string.sales_filter_by_date))
            .assertIsDisplayed()
    }
}
