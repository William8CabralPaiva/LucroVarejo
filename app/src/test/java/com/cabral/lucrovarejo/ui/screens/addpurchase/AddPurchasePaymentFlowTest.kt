package com.cabral.lucrovarejo.ui.screens.addpurchase

import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.domain.payment.usecase.GetFilteredCurrentTariffUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetSaleTariffUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetTerminalTariffsUseCase
import com.cabral.lucrovarejo.domain.payment.usecase.GetTerminalsUseCase
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.domain.product.ProductRepository
import com.cabral.lucrovarejo.domain.product.usecase.GetProductsUseCase
import com.cabral.lucrovarejo.domain.purchase.MonthlyPurchaseSummary
import com.cabral.lucrovarejo.domain.purchase.PurchaseModel
import com.cabral.lucrovarejo.domain.purchase.PurchaseRepository
import com.cabral.lucrovarejo.domain.purchase.usecase.CreatePurchaseUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.DeletePurchaseUseCase
import com.cabral.lucrovarejo.domain.purchase.usecase.UpdatePurchaseUseCase
import java.math.BigDecimal
import java.util.Calendar
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
class AddPurchasePaymentFlowTest {
    private val dispatcher = StandardTestDispatcher()
    private val product = ProductModel(
        key = "p1",
        cod = "A-1",
        name = "Bolsa",
        cost = BigDecimal("50.00"),
        description = ""
    )
    private val terminal = PaymentTerminalModel(
        name = "Maquininha A",
        active = true,
        enabledMethods = setOf(
            TerminalPaymentType.PIX,
            TerminalPaymentType.DEBIT,
            TerminalPaymentType.CREDIT,
            TerminalPaymentType.CASH
        ),
        brands = listOf("Visa", "Elo"),
        key = "t1"
    )
    private val archived = terminal.copy(name = "Antiga", active = false, key = "t2")
    private val purchases = RecordingPurchaseRepository()
    private lateinit var viewModel: AddPurchaseViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val tariffs = mapOf(
            "t1" to listOf(
                tariff("tc1", TerminalPaymentType.CREDIT, "Visa", 3, 300, date(2026, 1, 1)),
                tariff("tc2", TerminalPaymentType.CREDIT, "Visa", 3, 250, date(2026, 6, 1)),
                tariff("tc3", TerminalPaymentType.CREDIT, "Visa", 6, 400, date(2026, 6, 1)),
                tariff("td1", TerminalPaymentType.DEBIT, "Visa", null, 150, date(2026, 1, 1)),
                tariff("tp1", TerminalPaymentType.PIX, null, null, 99, date(2026, 1, 1)),
                tariff("tcash", TerminalPaymentType.CASH, null, null, 50, date(2026, 1, 1))
            )
        )
        viewModel = AddPurchaseViewModel(
            getProducts = GetProductsUseCase(object : ProductRepository {
                override suspend fun createProduct(product: ProductModel) = product
                override suspend fun updateProduct(product: ProductModel) = product
                override suspend fun deleteProduct(key: String) = Unit
                override fun observeProducts(): Flow<List<ProductModel>> = flowOf(listOf(product))
            }),
            createPurchase = CreatePurchaseUseCase(purchases),
            updatePurchase = UpdatePurchaseUseCase(purchases),
            deletePurchase = DeletePurchaseUseCase(purchases),
            getTerminals = GetTerminalsUseCase(FakeTerminalRepository(listOf(terminal, archived))),
            getTerminalTariffs = GetTerminalTariffsUseCase(FakeTariffRepository(tariffs)),
            getSaleTariff = GetSaleTariffUseCase(GetFilteredCurrentTariffUseCase())
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun date(year: Int, month: Int, day: Int): Date = Calendar.getInstance().apply {
        clear()
        set(year, month - 1, day)
    }.time

    private fun tariff(
        key: String,
        method: TerminalPaymentType,
        brand: String?,
        installments: Int?,
        rateBps: Int,
        from: Date
    ) = PaymentMachineModel("t1", method, brand, installments, rateBps, from, key = key)

    private fun fillBasics(dateMillis: Long) {
        viewModel.onProductSelected(productDisplayName(product))
        viewModel.onSalePriceChanged("100,00")
        viewModel.onSaleDateChanged(dateMillis)
    }

    @Test
    fun cascadingSpinnersOnlyListActiveTerminalsAndBrandsWithTariff() = runTest {
        advanceUntilIdle()
        viewModel.onPaymentMethodSelected(PaymentMethods.CREDIT)
        advanceUntilIdle()

        assertEquals(listOf("Maquininha A"), viewModel.uiState.value.terminalOptions.map { it.name })
        // A única maquininha ativa já vem selecionada, então as bandeiras já estão disponíveis.
        assertEquals(listOf("Visa"), viewModel.uiState.value.brandOptions)

        viewModel.onTerminalSelected("Maquininha A")
        advanceUntilIdle()
        assertEquals(listOf("Visa"), viewModel.uiState.value.brandOptions)

        viewModel.onBrandSelected("Visa")
        assertEquals(listOf(3, 6), viewModel.uiState.value.installmentOptions)
        assertFalse(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun changingPaymentMethodClearsFollowingSelections() = runTest {
        advanceUntilIdle()
        viewModel.onPaymentMethodSelected(PaymentMethods.CREDIT)
        viewModel.onTerminalSelected("Maquininha A")
        advanceUntilIdle()
        viewModel.onBrandSelected("Visa")
        viewModel.onInstallmentsSelected(3)

        viewModel.onPaymentMethodSelected(PaymentMethods.DEBIT)

        val form = viewModel.uiState.value.form
        // Só há uma maquininha ativa com Débito, então ela é preenchida sozinha.
        assertEquals("t1", form.terminalId)
        assertNull(form.brand)
        assertNull(form.installments)
    }

    @Test
    fun singleActiveTerminalIsSelectedAutomatically() = runTest {
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.form.terminalId)

        viewModel.onPaymentMethodSelected(PaymentMethods.CREDIT)
        advanceUntilIdle()

        val form = viewModel.uiState.value.form
        assertEquals("t1", form.terminalId)
        assertEquals("Maquininha A", form.terminalName)
        assertEquals(listOf("Visa"), viewModel.uiState.value.brandOptions)
    }

    @Test
    fun cashDoesNotSelectTerminal() = runTest {
        advanceUntilIdle()
        viewModel.onPaymentMethodSelected(PaymentMethods.CASH)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.form.terminalId)
    }

    @Test
    fun creditSaleSavesTariffSnapshotInCents() = runTest {
        advanceUntilIdle()
        fillBasics(date(2026, 7, 10).time)
        viewModel.onQuantityChanged(2)
        viewModel.onPaymentMethodSelected(PaymentMethods.CREDIT)
        viewModel.onTerminalSelected("Maquininha A")
        advanceUntilIdle()
        viewModel.onBrandSelected("Visa")
        viewModel.onInstallmentsSelected(3)
        assertTrue(viewModel.uiState.value.canSubmit)

        viewModel.savePurchase()
        advanceUntilIdle()

        val saved = requireNotNull(purchases.created)
        assertEquals("t1", saved.terminalId)
        assertEquals("Maquininha A", saved.terminalName)
        assertEquals("Visa", saved.brand)
        assertEquals(3, saved.installments)
        assertEquals("tc2", saved.tariffId)
        assertEquals(250, saved.tariffRateBps)
        assertEquals(500L, saved.tariffAmountCents)
    }

    @Test
    fun retroactiveDateUsesOlderTariffAndDateChangeRevalidatesSelection() = runTest {
        advanceUntilIdle()
        fillBasics(date(2026, 7, 10).time)
        viewModel.onPaymentMethodSelected(PaymentMethods.CREDIT)
        viewModel.onTerminalSelected("Maquininha A")
        advanceUntilIdle()
        viewModel.onBrandSelected("Visa")
        viewModel.onInstallmentsSelected(6)
        assertEquals(400, viewModel.uiState.value.appliedTariff.rateBps)

        viewModel.onSaleDateChanged(date(2026, 3, 10).time)

        val state = viewModel.uiState.value
        assertEquals("Visa", state.form.brand)
        assertNull(state.form.installments)
        assertEquals(R.string.purchase_tariff_selection_cleared, state.tariffNotice)
        assertEquals(AppliedTariff.NONE, state.appliedTariff)

        viewModel.onInstallmentsSelected(3)
        assertEquals(300, viewModel.uiState.value.appliedTariff.rateBps)
    }

    @Test
    fun cashUsesStoredCashTariffAndPixWithoutTariffIsSavedWithoutFee() = runTest {
        advanceUntilIdle()
        fillBasics(date(2026, 7, 10).time)
        viewModel.onPaymentMethodSelected(PaymentMethods.CASH)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.canSubmit)
        assertEquals(50, viewModel.uiState.value.appliedTariff.rateBps)

        fillBasics(date(2025, 12, 1).time)
        assertEquals(AppliedTariff.NONE, viewModel.uiState.value.appliedTariff)
        viewModel.savePurchase()
        advanceUntilIdle()

        val saved = requireNotNull(purchases.created)
        assertEquals(0, saved.tariffRateBps)
        assertEquals(0L, saved.tariffAmountCents)
        assertNull(saved.tariffId)
        assertNull(saved.terminalId)
    }

    @Test
    fun editingKeepsSelectionAndRefreshesTariffToCurrentRate() = runTest {
        advanceUntilIdle()
        val saved = PurchaseModel(
            key = "k1", cod = "A-1", name = "Bolsa", cost = 5000, description = "",
            salePrice = 10000, quantity = 1, saleDate = date(2026, 7, 10),
            paymentMethod = PaymentMethods.PIX, terminalId = "t1", terminalName = "Maquininha A",
            tariffRateBps = 777, tariffId = "old", tariffAmountCents = 777
        )

        viewModel.loadPurchase(saved)
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertEquals("t1", state.form.terminalId)
        assertEquals("Maquininha A", state.form.terminalName)
        assertEquals(99, state.appliedTariff.rateBps)
        assertEquals("tp1", state.appliedTariff.tariffId)
        assertTrue(state.canSubmit)

        viewModel.savePurchase()
        advanceUntilIdle()

        val updated = requireNotNull(purchases.updated)
        assertEquals(99, updated.tariffRateBps)
        assertEquals("tp1", updated.tariffId)
        assertEquals(99L, updated.tariffAmountCents)
    }

    @Test
    fun editingSaleAndChangingToDebitOffersBrandsWithSingleTerminal() = runTest {
        advanceUntilIdle()
        val pix = PurchaseModel(
            key = "k1", cod = "A-1", name = "Bolsa", cost = 5000, description = "",
            salePrice = 10000, quantity = 1, saleDate = date(2026, 7, 10),
            paymentMethod = PaymentMethods.PIX, terminalId = "t1", terminalName = "Maquininha A"
        )
        viewModel.loadPurchase(pix)
        advanceUntilIdle()

        viewModel.onPaymentMethodSelected(PaymentMethods.DEBIT)
        advanceUntilIdle()

        assertEquals("t1", viewModel.uiState.value.form.terminalId)
        assertEquals(listOf("Visa"), viewModel.uiState.value.brandOptions)
    }

    @Test
    fun editingSaleWithSelectedDebitStillOffersBrands() = runTest {
        advanceUntilIdle()
        val debit = PurchaseModel(
            key = "k1", cod = "A-1", name = "Bolsa", cost = 5000, description = "",
            salePrice = 10000, quantity = 1, saleDate = date(2026, 7, 10),
            paymentMethod = PaymentMethods.DEBIT, terminalId = "t1", terminalName = "Maquininha A",
            brand = "Visa"
        )
        viewModel.loadPurchase(debit)
        advanceUntilIdle()

        assertEquals(listOf("Visa"), viewModel.uiState.value.brandOptions)
        assertEquals("Visa", viewModel.uiState.value.form.brand)
    }

    private class RecordingPurchaseRepository : PurchaseRepository {
        var created: PurchaseModel? = null
        var updated: PurchaseModel? = null
        override suspend fun updatePurchase(purchase: PurchaseModel): PurchaseModel {
            updated = purchase
            return purchase
        }
        override suspend fun deletePurchase(key: String) = Unit
        override suspend fun createPurchase(purchase: PurchaseModel): PurchaseModel {
            created = purchase
            return purchase.copy(key = "new")
        }

        override fun observeMonthlySummary(start: Date, end: Date) = flowOf(MonthlyPurchaseSummary())
        override fun observeLatestPurchases(limit: Int): Flow<List<PurchaseModel>> = flowOf(emptyList())
    }
}
