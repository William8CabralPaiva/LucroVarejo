package com.cabral.lucrovarejo.ui.screens.addpurchase

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import com.cabral.lucrovarejo.domain.product.usecase.GetProductsUseCase
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.purchase.usecase.CreatePurchaseUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.DeletePurchaseUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.UpdatePurchaseUseCase
import com.cabral.lucrovarejo.ui.components.DELETE_PRODUCT_BUTTON_TAG
import com.cabral.lucrovarejo.ui.components.SAVE_PRODUCT_BUTTON_TAG
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.math.BigDecimal
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AddPurchaseScreenRouteTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val product = ProductModel(
        key = "product-1",
        cod = "A-1",
        name = "Bolsa Feminina",
        cost = BigDecimal("50.00"),
        description = "Bolsa"
    )
    private val purchase = PurchaseModel(
        key = "purchase-key",
        cod = "A-1",
        name = "Bolsa Feminina",
        cost = 5000,
        description = "Bolsa",
        salePrice = 8990,
        quantity = 2,
        saleDate = Date(1_700_000_000_000),
        paymentMethod = PaymentMethods.PIX
    )
    private val repository = FakePurchaseRepository()
    private val viewModel = AddPurchaseViewModel(
        getProducts = GetProductsUseCase(object : ProductRepository {
            override suspend fun createProduct(product: ProductModel) = product
            override suspend fun updateProduct(product: ProductModel) = product
            override suspend fun deleteProduct(key: String) = Unit
            override fun observeProducts(): Flow<List<ProductModel>> = flowOf(listOf(product))
        }),
        createPurchase = CreatePurchaseUseCase(repository),
        updatePurchase = UpdatePurchaseUseCase(repository),
        deletePurchase = DeletePurchaseUseCase(repository)
    )

    private fun string(id: Int) = RuntimeEnvironment.getApplication().getString(id)

    @Test
    fun savingNewPurchaseCreatesItAndShowsSuccessMessage() {
        viewModel.onProductSelected(productDisplayName(product))
        viewModel.onSalePriceChanged("89,90")
        viewModel.onPaymentMethodSelected(PaymentMethods.PIX)
        composeRule.setContent {
            LucroVarejoTheme { AddPurchaseScreen(viewModel = viewModel) }
        }

        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG).performScrollTo().performClick()

        composeRule.waitUntil(5_000) { repository.created != null }
        composeRule.onNodeWithText(string(R.string.purchase_create_success)).assertIsDisplayed()
    }

    @Test
    fun editingPurchaseFinishesAfterSave() {
        var finished = 0
        composeRule.setContent {
            LucroVarejoTheme {
                AddPurchaseScreen(purchase = purchase, onFinished = { finished++ }, viewModel = viewModel)
            }
        }

        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG).performScrollTo().performClick()

        composeRule.waitUntil(5_000) { finished == 1 }
        assertNotNull(repository.updated)
    }

    @Test
    fun confirmingDeleteDialogDeletesPurchaseAndFinishes() {
        var finished = 0
        composeRule.setContent {
            LucroVarejoTheme {
                AddPurchaseScreen(purchase = purchase, onFinished = { finished++ }, viewModel = viewModel)
            }
        }

        composeRule.onNodeWithTag(DELETE_PRODUCT_BUTTON_TAG).performScrollTo().performClick()
        composeRule.onNodeWithText(string(R.string.purchase_delete_dialog_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.purchase_delete_dialog_confirm)).performClick()

        composeRule.waitUntil(5_000) { finished == 1 }
        assertEquals("purchase-key", repository.deletedKey)
    }

    @Test
    fun cancellingDeleteDialogKeepsPurchase() {
        composeRule.setContent {
            LucroVarejoTheme { AddPurchaseScreen(purchase = purchase, viewModel = viewModel) }
        }

        composeRule.onNodeWithTag(DELETE_PRODUCT_BUTTON_TAG).performScrollTo().performClick()
        composeRule.onNodeWithText(string(R.string.date_picker_cancel)).performClick()

        composeRule.waitForIdle()
        assertEquals(null, repository.deletedKey)
        assertEquals(false, viewModel.uiState.value.showDeleteDialog)
    }

    private class FakePurchaseRepository : PurchaseRepository {
        var created: PurchaseModel? = null
        var updated: PurchaseModel? = null
        var deletedKey: String? = null

        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel {
            created = purchase
            return purchase.copy(key = "generated")
        }

        override suspend fun updatePurchase(purchase: PurchaseModel): PurchaseModel {
            updated = purchase
            return purchase
        }

        override suspend fun deletePurchase(key: String) {
            deletedKey = key
        }

        override fun observeMonthlySummary(start: Date, end: Date) =
            flowOf(MonthlyPurchaseSummary())

        override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>> =
            flowOf(emptyList())
    }
}
