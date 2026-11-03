package com.cabral.lucrovarejo.ui.screens.addproduct

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import com.cabral.lucrovarejo.domain.product.usecase.CreateProductUseCase
import com.cabral.lucrovarejo.domain.product.usecase.DeleteProductUseCase
import com.cabral.lucrovarejo.domain.product.usecase.UpdateProductUseCase
import com.cabral.lucrovarejo.ui.components.DELETE_PRODUCT_BUTTON_TAG
import com.cabral.lucrovarejo.ui.components.SAVE_PRODUCT_BUTTON_TAG
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.math.BigDecimal
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AddProductScreenRouteTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val repository = FakeProductRepository()
    private val viewModel = AddProductViewModel(
        CreateProductUseCase(repository),
        UpdateProductUseCase(repository),
        DeleteProductUseCase(repository)
    )
    private val existingProduct = ProductModel(
        key = "product-key",
        cod = "A-1",
        name = "Bolsa",
        cost = BigDecimal("89.90"),
        description = "Couro"
    )

    private fun string(id: Int, vararg args: Any) =
        RuntimeEnvironment.getApplication().getString(id, *args)

    @Test
    fun savingNewProductCreatesItAndShowsSuccessMessage() {
        viewModel.onNameChanged("Produto")
        viewModel.onCostChanged("10.00")
        composeRule.setContent {
            LucroVarejoTheme { AddProductScreen(viewModel = viewModel) }
        }

        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG).performScrollTo().performClick()

        composeRule.waitUntil(5_000) { repository.created != null }
        composeRule.onNodeWithText(string(R.string.product_create_success)).assertIsDisplayed()
    }

    @Test
    fun editingProductLoadsItAndFinishesAfterSave() {
        var finished = 0
        composeRule.setContent {
            LucroVarejoTheme {
                AddProductScreen(product = existingProduct, onFinished = { finished++ }, viewModel = viewModel)
            }
        }
        composeRule.onNodeWithText("Bolsa").assertIsDisplayed()

        composeRule.onNodeWithTag(SAVE_PRODUCT_BUTTON_TAG).performScrollTo().performClick()

        composeRule.waitUntil(5_000) { finished == 1 }
        assertEquals(existingProduct, repository.updated)
    }

    @Test
    fun confirmingDeleteDialogDeletesProductAndFinishes() {
        var finished = 0
        composeRule.setContent {
            LucroVarejoTheme {
                AddProductScreen(product = existingProduct, onFinished = { finished++ }, viewModel = viewModel)
            }
        }

        composeRule.onNodeWithTag(DELETE_PRODUCT_BUTTON_TAG).performScrollTo().performClick()
        composeRule.onNodeWithText(string(R.string.product_delete_dialog_title)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.product_delete_dialog_confirm)).performClick()

        composeRule.waitUntil(5_000) { finished == 1 }
        assertEquals("product-key", repository.deletedKey)
    }

    @Test
    fun cancellingDeleteDialogKeepsProduct() {
        composeRule.setContent {
            LucroVarejoTheme { AddProductScreen(product = existingProduct, viewModel = viewModel) }
        }

        composeRule.onNodeWithTag(DELETE_PRODUCT_BUTTON_TAG).performScrollTo().performClick()
        composeRule.onNodeWithText(string(R.string.date_picker_cancel)).performClick()

        composeRule.waitForIdle()
        assertEquals(null, repository.deletedKey)
        assertEquals(false, viewModel.uiState.value.showDeleteDialog)
    }

    private class FakeProductRepository : ProductRepository {
        var created: ProductModel? = null
        var updated: ProductModel? = null
        var deletedKey: String? = null

        override suspend fun createProduct(product: ProductModel): ProductModel {
            created = product
            return product.copy(key = "generated")
        }

        override suspend fun updateProduct(product: ProductModel): ProductModel {
            updated = product
            return product
        }

        override suspend fun deleteProduct(key: String) {
            deletedKey = key
        }

        override fun observeProducts() = flowOf(emptyList<ProductModel>())
    }
}
