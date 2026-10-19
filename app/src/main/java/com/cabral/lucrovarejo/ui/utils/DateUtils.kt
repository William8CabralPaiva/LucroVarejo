package com.cabral.lucrovarejo.ui.utils

import java.util.Calendar
import java.util.TimeZone

object DateUtils {

    private val UTC_TIME_ZONE: TimeZone = TimeZone.getTimeZone("UTC")

    fun todayUtcMillis(): Long = Calendar.getInstance(
        TimeZone.getTimeZone("UTC")
    ).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    fun mergeDateWithExistingTime(
        dateMillis: Long,
        existingDateTimeMillis: Long
    ): Long {
        val selectedDate = Calendar.getInstance(UTC_TIME_ZONE).apply {
            timeInMillis = dateMillis
        }
        val existingDateTime = Calendar.getInstance(UTC_TIME_ZONE).apply {
            timeInMillis = existingDateTimeMillis
        }
        selectedDate.set(Calendar.HOUR_OF_DAY, existingDateTime.get(Calendar.HOUR_OF_DAY))
        selectedDate.set(Calendar.MINUTE, existingDateTime.get(Calendar.MINUTE))
        selectedDate.set(Calendar.SECOND, existingDateTime.get(Calendar.SECOND))
        selectedDate.set(Calendar.MILLISECOND, existingDateTime.get(Calendar.MILLISECOND))
        return selectedDate.timeInMillis
    }

}