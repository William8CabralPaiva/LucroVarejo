package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import java.util.Calendar
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetSaleTariffUseCaseTest {
    private val useCase = GetSaleTariffUseCase(GetFilteredCurrentTariffUseCase())

    private fun day(year: Int, month: Int, day: Int, hour: Int = 0): Date =
        Calendar.getInstance().apply {
            clear()
            set(year, month - 1, day, hour, 0, 0)
        }.time

    private fun tariff(
        rateBps: Int,
        effectiveFrom: Date,
        method: TerminalPaymentType = TerminalPaymentType.CREDIT,
        brandId: String? = "Visa",
        installments: Int? = 1
    ) = PaymentMachineModel(
        terminalId = "t1",
        method = method,
        brandId = brandId,
        installments = installments,
        rateBps = rateBps,
        effectiveFrom = effectiveFrom,
        key = "k$rateBps"
    )

    @Test
    fun usesLatestTariffEffectiveOnSaleDate() {
        val tariffs = listOf(tariff(300, day(2026, 1, 1)), tariff(250, day(2026, 3, 1)))

        val today = useCase(tariffs, PaymentMethods.CREDIT, "Visa", 1, day(2026, 4, 10, 15))
        val retroactive = useCase(tariffs, PaymentMethods.CREDIT, "Visa", 1, day(2026, 2, 10))

        assertEquals(250, today?.rateBps)
        assertEquals(300, retroactive?.rateBps)
    }

    @Test
    fun tariffStartingTodayAppliesToSalesOfToday() {
        val tariffs = listOf(tariff(250, day(2026, 4, 10)))

        assertEquals(250, useCase(tariffs, PaymentMethods.CREDIT, "Visa", 1, day(2026, 4, 10, 9))?.rateBps)
    }

    @Test
    fun returnsNullWithoutTariffOnDateOrForOtherKey() {
        val tariffs = listOf(tariff(250, day(2026, 3, 1)))

        assertNull(useCase(tariffs, PaymentMethods.CREDIT, "Visa", 1, day(2026, 2, 1)))
        assertNull(useCase(tariffs, PaymentMethods.CREDIT, "Visa", 2, day(2026, 4, 1)))
        assertNull(useCase(tariffs, PaymentMethods.DEBIT, "Visa", null, day(2026, 4, 1)))
    }

    @Test
    fun resolvesCashTariffWithoutBrandOrInstallments() {
        val tariffs = listOf(tariff(100, day(2026, 1, 1), TerminalPaymentType.CASH, null, null))

        assertEquals(100, useCase(tariffs, PaymentMethods.CASH, null, null, day(2026, 4, 1))?.rateBps)
    }

    @Test
    fun calculatesTariffAmountInCentsRoundingHalfUp() {
        assertEquals(249L, calculateTariffAmountCents(10_000, 249))
        assertEquals(3L, calculateTariffAmountCents(101, 249))
        assertEquals(1L, calculateTariffAmountCents(50, 249))
        assertEquals(0L, calculateTariffAmountCents(10_000, 0))
    }
}
