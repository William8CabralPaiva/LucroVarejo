package com.cabral.lucrovarejo.ui.screens.addproduct

import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.product.ProductFailure
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import com.cabral.lucrovarejo.domain.product.usecase.CreateProductUseCase
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
        viewModel = AddProductViewModel(CreateProductUseCase(repository))
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

    private class FakeProductRepository : ProductRepository {
        var createdProduct: ProductModel? = null
        var failure: ProductFailure? = null

        override suspend fun createProduct(product: ProductModel): ProductModel {
            failure?.let { throw it }
            createdProduct = product
            return product.copy(id = "generated-product-id")
        }

        override fun observeProducts() = flowOf(emptyList<ProductModel>())
    }
}
