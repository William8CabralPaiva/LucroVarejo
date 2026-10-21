package com.cabral.lucrovarejo.ui.screens.addproduct

import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.product.ProductFailure
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import com.cabral.lucrovarejo.domain.product.usecase.CreateProductUseCase
import com.cabral.lucrovarejo.domain.product.usecase.DeleteProductUseCase
import com.cabral.lucrovarejo.domain.product.usecase.UpdateProductUseCase
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddProductViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeProductRepository
    private lateinit var viewModel: AddProductViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeProductRepository()
        viewModel = AddProductViewModel(
            CreateProductUseCase(repository),
            UpdateProductUseCase(repository),
            DeleteProductUseCase(repository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun validatesRequiredNameAndCost() = runTest {
        viewModel.saveProduct()

        assertEquals(R.string.product_name_required, viewModel.uiState.value.nameError)
        assertEquals(R.string.product_cost_required, viewModel.uiState.value.costError)
        assertNull(repository.createdProduct)
    }

    @Test
    fun savesProductWithOptionalFieldsAndMoneyConvertedToDecimal() = runTest {
        viewModel.onNameChanged("  Produto teste  ")
        viewModel.onCostChanged("12,34")
        val effect = async { viewModel.effects.first() }

        viewModel.saveProduct()
        advanceUntilIdle()

        assertEquals(
            ProductModel(
                cod = "",
                name = "Produto teste",
                cost = BigDecimal("12.34"),
                description = ""
            ),
            repository.createdProduct
        )
        assertEquals(Unit, effect.await())
        assertEquals("", viewModel.uiState.value.name)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun surfacesFirebaseFailureAndStopsLoading() = runTest {
        repository.failure = ProductFailure(ProductFailure.Reason.NETWORK)
        viewModel.onNameChanged("Produto")
        viewModel.onCostChanged("10.00")

        viewModel.saveProduct()
        advanceUntilIdle()

        assertEquals(R.string.product_create_error_network, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun loadProductFillsFieldsAndEntersEditMode() = runTest {
        viewModel.loadProduct(existingProduct)

        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertEquals("Bolsa", state.name)
        assertEquals("A-1", state.cod)
        assertEquals("89.90", state.cost)
        assertEquals("Couro", state.description)
    }

    @Test
    fun savingInEditModeUpdatesProductByKeyAndCompletes() = runTest {
        viewModel.loadProduct(existingProduct)
        viewModel.onNameChanged("Bolsa Nova")
        val completion = async { viewModel.completions.first() }

        viewModel.saveProduct()
        advanceUntilIdle()

        assertEquals(existingProduct.copy(name = "Bolsa Nova"), repository.updatedProduct)
        assertNull(repository.createdProduct)
        assertEquals(Unit, completion.await())
    }

    @Test
    fun deleteRequiresConfirmationBeforeDeletingByKey() = runTest {
        viewModel.loadProduct(existingProduct)
        val completion = async { viewModel.completions.first() }

        viewModel.onDeleteRequested()
        assertTrue(viewModel.uiState.value.showDeleteDialog)
        assertNull(repository.deletedKey)

        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals("product-key", repository.deletedKey)
        assertFalse(viewModel.uiState.value.showDeleteDialog)
        assertEquals(Unit, completion.await())
    }

    @Test
    fun dismissingDeleteDialogKeepsProduct() = runTest {
        viewModel.loadProduct(existingProduct)
        viewModel.onDeleteRequested()

        viewModel.onDeleteDismissed()

        assertFalse(viewModel.uiState.value.showDeleteDialog)
        assertNull(repository.deletedKey)
    }

    private val existingProduct = ProductModel(
        key = "product-key",
        cod = "A-1",
        name = "Bolsa",
        cost = BigDecimal("89.90"),
        description = "Couro"
    )
    private class FakeProductRepository : ProductRepository {
        var createdProduct: ProductModel? = null
        var updatedProduct: ProductModel? = null
        var deletedKey: String? = null
        var failure: ProductFailure? = null

        override suspend fun createProduct(product: ProductModel): ProductModel {
            failure?.let { throw it }
            createdProduct = product
            return product.copy(key = "generated-product-id")
        }

        override suspend fun updateProduct(product: ProductModel): ProductModel {
            failure?.let { throw it }
            updatedProduct = product
            return product
        }

        override suspend fun deleteProduct(key: String) {
            failure?.let { throw it }
            deletedKey = key
        }

        override fun observeProducts() = flowOf(emptyList<ProductModel>())
    }
}
