package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetFilteredCurrentTariffUseCaseTest {
    private val useCase = GetFilteredCurrentTariffUseCase()

    private fun tariff(effectiveFrom: Date, rateBps: Int) = PaymentMachineModel(
        terminalId = "terminal-1",
        method = TerminalPaymentType.CREDIT,
        brandId = "visa",
        installments = 1,
        rateBps = rateBps,
        effectiveFrom = effectiveFrom
    )

    @Test
    fun picksLatestEffectiveFromNotAfterDate() {
        val older = tariff(Date(1_000), rateBps = 300)
        val newer = tariff(Date(2_000), rateBps = 400)
        val future = tariff(Date(5_000), rateBps = 999)

        val result = useCase(listOf(older, newer, future), date = Date(3_000))

        assertEquals(newer, result)
    }

    @Test
    fun returnsNullWhenAllTariffsAreInTheFuture() {
        val future = tariff(Date(5_000), rateBps = 400)

        val result = useCase(listOf(future), date = Date(1_000))

        assertNull(result)
    }

    @Test
    fun resolvesCorrectlyEvenWhenRetroactiveTariffIsInsertedOutOfOrder() {
        val original = tariff(Date(1_000), rateBps = 300)
        val retroactiveCorrection = tariff(Date(500), rateBps = 250)

        // A tarifa retroativa foi inserida depois (ordem de lista nao importa),
        // mas na data 1_500 a vigente ainda eh a de effectiveFrom=1_000.
        val result = useCase(listOf(original, retroactiveCorrection), date = Date(1_500))

        assertEquals(original, result)
    }

    @Test
    fun picksExactMatchWhenDateEqualsEffectiveFrom() {
        val tariff = tariff(Date(1_000), rateBps = 349)

        val result = useCase(listOf(tariff), date = Date(1_000))

        assertEquals(tariff, result)
    }

    @Test
    fun breaksTieByPickingAnyMaxWhenEffectiveFromRepeats() {
        // Em tese nao deveria haver empate (checado na escrita), mas a funcao deve
        // permanecer deterministica: maxByOrNull retorna o primeiro encontrado com o maior valor.
        val first = tariff(Date(1_000), rateBps = 300)
        val duplicateDate = tariff(Date(1_000), rateBps = 350)

        val result = useCase(listOf(first, duplicateDate), date = Date(2_000))

        assertEquals(first, result)
    }

    @Test
    fun resolvesBpsPrecisionWithoutRounding() {
        val tariff = tariff(Date(1_000), rateBps = 249)

        val result = useCase(listOf(tariff), date = Date(1_500))

        assertEquals(249, result?.rateBps)
    }
}
