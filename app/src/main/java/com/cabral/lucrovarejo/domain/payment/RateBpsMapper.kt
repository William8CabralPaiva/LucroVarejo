package com.cabral.lucrovarejo.domain.payment

import java.math.BigDecimal
import java.math.RoundingMode

private const val RATE_SCALE = 2
private const val MAX_RATE_PERCENT = 100L

/** Converte a tarifa em % (BigDecimal, ex.: 2.49) para basis points (ex.: 249). */
fun BigDecimal.toRateBps(): Int = setScale(RATE_SCALE, RoundingMode.UNNECESSARY)
    .movePointRight(RATE_SCALE)
    .intValueExact()

/** Converte basis points (ex.: 249) de volta para % (BigDecimal, ex.: 2.49). */
fun Int.bpsToRate(): BigDecimal = BigDecimal.valueOf(toLong(), RATE_SCALE)

/** Converte o texto digitado (aceita virgula) em basis points; null se invalido ou fora de 0..100%. */
fun parseRatePercentToBps(text: String): Int? {
    val percent = text.replace(',', '.').toBigDecimalOrNull()
        ?.takeIf { it >= BigDecimal.ZERO && it <= BigDecimal.valueOf(MAX_RATE_PERCENT) }
    return percent?.let { runCatching { it.toRateBps() }.getOrNull() }
}
