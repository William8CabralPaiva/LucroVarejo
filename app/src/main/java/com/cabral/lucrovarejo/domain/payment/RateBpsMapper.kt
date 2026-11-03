package com.cabral.lucrovarejo.domain.payment

import java.math.BigDecimal
import java.math.RoundingMode

private const val RATE_SCALE = 2

/** Converte a tarifa em % (BigDecimal, ex.: 2.49) para basis points (ex.: 249). */
fun BigDecimal.toRateBps(): Int = setScale(RATE_SCALE, RoundingMode.UNNECESSARY)
    .movePointRight(RATE_SCALE)
    .intValueExact()

/** Converte basis points (ex.: 249) de volta para % (BigDecimal, ex.: 2.49). */
fun Int.bpsToRate(): BigDecimal = BigDecimal.valueOf(toLong(), RATE_SCALE)
