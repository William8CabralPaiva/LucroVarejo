package com.cabral.lucrovarejo.ui.screens.addpurchase

import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.product.ProductFailure
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import com.cabral.lucrovarejo.domain.product.usecase.GetProductsUseCase
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.purchase.usecase.CreatePurchaseUseCase
import java.math.BigDecimal
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AddPurchaseViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var purchaseRepository: FakePurchaseRepository
    private lateinit var viewModel: AddPurchaseViewModel
    private val product = ProductModel(
        id = "product-1",
        cod = "A-1",
        name = "Bolsa Feminina",
        cost = BigDecimal("50.00"),
        description = "Bolsa"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        purchaseRepository = FakePurchaseRepository()
        viewModel = AddPurchaseViewModel(
            getProducts = GetProductsUseCase(FakeProductRepository(listOf(product))),
            createPurchase = CreatePurchaseUseCase(purchaseRepository)
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsProductsFromServiceAndValidatesRequiredPurchaseFields() = runTest {
        advanceUntilIdle()

        assertEquals(listOf(product), viewModel.uiState.value.products)
        assertFalse(viewModel.uiState.value.isLoadingProducts)

        viewModel.savePurchase()

        assertEquals(
            R.string.purchase_sale_price_required,
            viewModel.uiState.value.form.salePriceError
        )
        assertNull(purchaseRepository.createdPurchase)
    }

    @Test
    fun showsLoadingUntilProductsFinishLoading() = runTest {
        val products = CompletableDeferred<List<ProductModel>>()
        viewModel = createViewModel(
            object : ProductRepository {
                override suspend fun createProduct(product: ProductModel): ProductModel = product
                override suspend fun getProducts(): List<ProductModel> = products.await()
            }
        )

        assertTrue(viewModel.uiState.value.isLoadingProducts)
        runCurrent()
        assertTrue(viewModel.uiState.value.isLoadingProducts)

        products.complete(listOf(product))
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoadingProducts)
        assertEquals(listOf(product), viewModel.uiState.value.products)
    }

    @Test
    fun exposesProductLoadFailureAndStopsLoading() = runTest {
        viewModel = createViewModel(
            object : ProductRepository {
                override suspend fun createProduct(product: ProductModel): ProductModel = product
                override suspend fun getProducts(): List<ProductModel> {
                    throw ProductFailure(ProductFailure.Reason.NETWORK)
                }
            }
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoadingProducts)
        assertEquals(
            R.string.purchase_load_products_error_network,
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun createsPurchaseWithSelectedProductPaymentAndAmountInCents() = runTest {
        advanceUntilIdle()
        val saleDateMillis = 1_735_689_600_000L
        viewModel.onProductSelected(productDisplayName(product))
        viewModel.onSalePriceChanged("89,90")
        viewModel.onQuantityChanged(2)
        viewModel.onPaymentMethodSelected(PaymentMethods.PIX)
        viewModel.onSaleDateChanged(saleDateMillis)
        val effect = async { viewModel.effects.first() }

        viewModel.savePurchase()
        advanceUntilIdle()

        assertEquals(
            PurchaseModel(
                cod = "A-1",
                name = "Bolsa Feminina",
                cost = 5000,
                description = "Bolsa",
                salePrice = 8990,
                quantity = 2,
                saleDate = Date(saleDateMillis),
                paymentMethods = listOf(PaymentMethods.PIX)
            ),
            purchaseRepository.createdPurchase
        )
        assertEquals(Unit, effect.await())
        assertEquals(BigDecimal("79.80"), viewModel.uiState.value.profit)
        assertTrue(viewModel.uiState.value.canSubmit)
    }

    private fun createViewModel(productRepository: ProductRepository): AddPurchaseViewModel =
        AddPurchaseViewModel(
            getProducts = GetProductsUseCase(productRepository),
            createPurchase = CreatePurchaseUseCase(purchaseRepository)
        )

    private class FakeProductRepository(
        private val products: List<ProductModel>
    ) : ProductRepository {
        override suspend fun createProduct(product: ProductModel): ProductModel = product
        override suspend fun getProducts(): List<ProductModel> = products
    }

    private class FakePurchaseRepository : PurchaseRepository {
        var createdPurchase: PurchaseModel? = null

        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel {
            createdPurchase = purchase
            return purchase.copy(id = "purchase-1")
        }
    }
}
