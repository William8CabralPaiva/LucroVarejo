package com.cabral.lucrovarejo.ui.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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

    // Formata como "06 OUTUBRO 2026".
    fun formatDayHeader(date: Date, timeZone: TimeZone = TimeZone.getDefault()): String {
        val locale = Locale.forLanguageTag("pt-BR")
        return SimpleDateFormat("dd MMMM yyyy", locale).apply {
            this.timeZone = timeZone
        }.format(date).uppercase(locale)
    }

    // O DatePicker devolve o dia à meia-noite UTC; converte para [início, início + 1 dia) no fuso local.
    fun localDayRangeFromUtcMillis(utcMillis: Long): Pair<Date, Date> {
        val utcDay = Calendar.getInstance(UTC_TIME_ZONE).apply { timeInMillis = utcMillis }
        val start = Calendar.getInstance().apply {
            clear()
            set(
                utcDay.get(Calendar.YEAR),
                utcDay.get(Calendar.MONTH),
                utcDay.get(Calendar.DAY_OF_MONTH),
                0,
                0,
                0
            )
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        return start.time to end.time
    }
}