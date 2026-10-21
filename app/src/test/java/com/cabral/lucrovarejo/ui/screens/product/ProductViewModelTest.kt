package com.cabral.lucrovarejo.ui.screens.product

import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import com.cabral.lucrovarejo.domain.product.usecase.GetProductsUseCase
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeProductRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        repository = FakeProductRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun keepsFirebaseDescendingOrderAndFiltersByNameOrCode() = runTest(dispatcher) {
        repository.products.value = listOf(product("2", "78920", "Tênis"), product("1", "78910", "Bolsa"))
        val viewModel = ProductViewModel(GetProductsUseCase(repository))
        advanceUntilIdle()

        assertEquals(listOf("2", "1"), viewModel.uiState.value.items.map { it.key })

        viewModel.onQueryChange("78910")
        advanceUntilIdle()
        assertEquals(listOf("Bolsa"), viewModel.uiState.value.items.map { it.name })

        viewModel.onQueryChange("tên")
        advanceUntilIdle()
        assertEquals(listOf("Tênis"), viewModel.uiState.value.items.map { it.name })
    }

    @Test
    fun appliesNewFirebaseEmissionsWithoutManualRefresh() = runTest(dispatcher) {
        val viewModel = ProductViewModel(GetProductsUseCase(repository))
        advanceUntilIdle()

        repository.products.value = listOf(product("new", "78930", "Produto novo"))
        advanceUntilIdle()

        assertEquals("Produto novo", viewModel.uiState.value.items.single().name)
    }

    @Test
    fun flowFailureStopsLoadingAndShowsError() = runTest(dispatcher) {
        val failing = object : ProductRepository by repository {
            override fun observeProducts() = kotlinx.coroutines.flow.flow<List<ProductModel>> {
                throw IllegalStateException("boom")
            }
        }
        val viewModel = ProductViewModel(GetProductsUseCase(failing))
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.hasError)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    private fun product(id: String, cod: String, name: String) = ProductModel(
        key = id,
        cod = cod,
        name = name,
        cost = BigDecimal("10.00"),
        description = ""
    )

    private class FakeProductRepository : ProductRepository {
    override suspend fun updateProduct(product: ProductModel): ProductModel = product
    override suspend fun deleteProduct(key: String) = Unit
        val products = MutableStateFlow(emptyList<ProductModel>())

        override suspend fun createProduct(product: ProductModel): ProductModel = product

        override fun observeProducts() = products
    }
}
