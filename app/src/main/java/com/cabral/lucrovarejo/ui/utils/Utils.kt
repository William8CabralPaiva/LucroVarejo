package com.cabral.lucrovarejo.ui.utils

import java.util.Calendar
import java.util.TimeZone

object Utils {
    fun todayUtcMillis(): Long = Calendar.getInstance(
        TimeZone.getTimeZone("UTC")
    ).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

}