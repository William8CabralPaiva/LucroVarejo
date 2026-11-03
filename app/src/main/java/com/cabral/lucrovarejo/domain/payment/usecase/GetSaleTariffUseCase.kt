package com.cabral.lucrovarejo.domain.payment.usecase

import com.cabral.lucrovarejo.domain.payment.PaymentMachineModel
import com.cabral.lucrovarejo.domain.payment.PaymentMethods
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

/**
 * Resolve a tarifa aplicada a uma venda: filtra pela chave (forma de pagamento + bandeira +
 * parcela) e escolhe a vigente na data da venda. Sem tarifa vigente retorna `null` (a venda
 * não é bloqueada; é salva sem taxa). Função pura, sem Android/Firebase.
 */
class GetSaleTariffUseCase @Inject constructor(
    private val getFilteredCurrentTariff: GetFilteredCurrentTariffUseCase
) {
    operator fun invoke(
        tariffs: List<PaymentMachineModel>,
        method: PaymentMethods,
        brandId: String?,
        installments: Int?,
        saleDate: Date
    ): PaymentMachineModel? = getFilteredCurrentTariff(
        tariffs = tariffs.filter {
            it.method == method.toTerminalType() &&
                it.brandId == brandId &&
                it.installments == installments
        },
        date = saleDate.endOfLocalDay()
    )
}

fun PaymentMethods.toTerminalType(): TerminalPaymentType = when (this) {
    PaymentMethods.CASH -> TerminalPaymentType.CASH
    PaymentMethods.PIX -> TerminalPaymentType.PIX
    PaymentMethods.DEBIT -> TerminalPaymentType.DEBIT
    PaymentMethods.CREDIT -> TerminalPaymentType.CREDIT
}

/**
 * `effectiveFrom` é uma data sem hora (início do dia local); comparar com o fim do dia da
 * venda faz uma tarifa que começa "hoje" valer para vendas de hoje.
 */
fun Date.endOfLocalDay(): Date = Calendar.getInstance().apply {
    time = this@endOfLocalDay
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}.time

/** Taxa em centavos inteiros: `totalCents * rateBps / 10_000`, arredondando `HALF_UP`. */
fun calculateTariffAmountCents(saleTotalCents: Long, rateBps: Int): Long =
    BigDecimal.valueOf(saleTotalCents)
        .multiply(BigDecimal.valueOf(rateBps.toLong()))
        .divide(BigDecimal.valueOf(BPS_DIVISOR), 0, RoundingMode.HALF_UP)
        .longValueExact()

private const val BPS_DIVISOR = 10_000L
