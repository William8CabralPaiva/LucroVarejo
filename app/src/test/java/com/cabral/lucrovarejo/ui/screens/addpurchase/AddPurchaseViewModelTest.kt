package com.cabral.lucrovarejo.ui.screens.addpurchase

import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.product.ProductFailure
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import com.cabral.lucrovarejo.domain.product.usecase.GetProductsUseCase
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.purchase.usecase.CreatePurchaseUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.DeletePurchaseUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.UpdatePurchaseUseCase
import com.cabral.lucrovarejo.ui.utils.DateUtils
import java.math.BigDecimal
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
        key = "product-1",
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
            getProducts = GetProductsUseCase(FakeProductRepository(flowOf(listOf(product)))),
            createPurchase = CreatePurchaseUseCase(purchaseRepository),
            updatePurchase = UpdatePurchaseUseCase(purchaseRepository),
            deletePurchase = DeletePurchaseUseCase(purchaseRepository)
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
        override suspend fun updateProduct(product: ProductModel): ProductModel = product
        override suspend fun deleteProduct(key: String) = Unit
                override suspend fun createProduct(product: ProductModel): ProductModel = product
                override fun observeProducts(): Flow<List<ProductModel>> = flow {
                    emit(products.await())
                }
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
        override suspend fun updateProduct(product: ProductModel): ProductModel = product
        override suspend fun deleteProduct(key: String) = Unit
                override suspend fun createProduct(product: ProductModel): ProductModel = product
                override fun observeProducts(): Flow<List<ProductModel>> = flow {
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
    fun updatesProductsWhenFirestoreEmitsANewList() = runTest {
        val products = MutableStateFlow(listOf(product))
        viewModel = createViewModel(
            object : ProductRepository {
        override suspend fun updateProduct(product: ProductModel): ProductModel = product
        override suspend fun deleteProduct(key: String) = Unit
                override suspend fun createProduct(product: ProductModel): ProductModel = product
                override fun observeProducts(): Flow<List<ProductModel>> = products
            }
        )
        advanceUntilIdle()

        val newProduct = product.copy(key = "product-2", name = "Carteira")
        products.value = listOf(product, newProduct)
        advanceUntilIdle()

        assertEquals(listOf(product, newProduct), viewModel.uiState.value.products)
    }

    @Test
    fun createsPurchaseWithSelectedProductPaymentAndAmountInCents() = runTest {
        advanceUntilIdle()
        val saleDateMillis = 1_735_689_600_000L
        val originalSaleDateMillis = viewModel.uiState.value.form.saleDateMillis
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
                saleDate = Date(
                    DateUtils.mergeDateWithExistingTime(saleDateMillis, originalSaleDateMillis)
                ),
                paymentMethod = PaymentMethods.PIX
            ),
            purchaseRepository.createdPurchase
        )
        assertEquals(Unit, effect.await())
        assertNull(viewModel.uiState.value.profit)
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun loadPurchaseFillsFormAndEntersEditMode() = runTest {
        advanceUntilIdle()

        viewModel.loadPurchase(existingPurchase)

        val state = viewModel.uiState.value
        assertTrue(state.isEditing)
        assertEquals("Bolsa Feminina", state.form.selectedProduct?.name)
        assertEquals("89.90", state.form.salePrice)
        assertEquals(2, state.form.quantity)
        assertEquals(PaymentMethods.PIX, state.form.paymentMethod)
        assertEquals(existingPurchase.saleDate.time, state.form.saleDateMillis)
    }

    @Test
    fun savingInEditModeUpdatesPurchaseByKeyAndCompletes() = runTest {
        advanceUntilIdle()
        viewModel.loadPurchase(existingPurchase)
        viewModel.onQuantityChanged(3)
        val completion = async { viewModel.completions.first() }

        viewModel.savePurchase()
        advanceUntilIdle()

        assertEquals(existingPurchase.copy(quantity = 3), purchaseRepository.updatedPurchase)
        assertNull(purchaseRepository.createdPurchase)
        assertEquals(Unit, completion.await())
    }

    @Test
    fun deleteRequiresConfirmationBeforeDeletingByKey() = runTest {
        advanceUntilIdle()
        viewModel.loadPurchase(existingPurchase)
        val completion = async { viewModel.completions.first() }

        viewModel.onDeleteRequested()
        assertTrue(viewModel.uiState.value.showDeleteDialog)
        assertNull(purchaseRepository.deletedKey)

        viewModel.confirmDelete()
        advanceUntilIdle()

        assertEquals("purchase-key", purchaseRepository.deletedKey)
        assertFalse(viewModel.uiState.value.showDeleteDialog)
        assertEquals(Unit, completion.await())
    }

    private val existingPurchase = PurchaseModel(
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
    private fun createViewModel(productRepository: ProductRepository): AddPurchaseViewModel =
        AddPurchaseViewModel(
            getProducts = GetProductsUseCase(productRepository),
            createPurchase = CreatePurchaseUseCase(purchaseRepository),
            updatePurchase = UpdatePurchaseUseCase(purchaseRepository),
            deletePurchase = DeletePurchaseUseCase(purchaseRepository)
        )

    private class FakeProductRepository(
        private val products: Flow<List<ProductModel>>
    ) : ProductRepository {
    override suspend fun updateProduct(product: ProductModel): ProductModel = product
    override suspend fun deleteProduct(key: String) = Unit
        override suspend fun createProduct(product: ProductModel): ProductModel = product
        override fun observeProducts(): Flow<List<ProductModel>> = products
    }

    private class FakePurchaseRepository : PurchaseRepository {
        var createdPurchase: PurchaseModel? = null
        var updatedPurchase: PurchaseModel? = null
        var deletedKey: String? = null

        override suspend fun updatePurchase(purchase: PurchaseModel): PurchaseModel {
            updatedPurchase = purchase
            return purchase
        }

        override suspend fun deletePurchase(key: String) {
            deletedKey = key
        }

        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel {
            createdPurchase = purchase
            return purchase.copy(key = "purchase-1")
        }

        override fun observeMonthlySummary(start: Date, end: Date) =
            flowOf(MonthlyPurchaseSummary())

        override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>> =
            flowOf(emptyList())
    }
}
