package com.cabral.lucrovarejo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.ui.theme.DarkPrimaryContainer
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun DateTextField(
    modifier: Modifier = Modifier,
    initialDateMillis: Long = todayUtcMillis(),
    @StringRes labelResId: Int = R.string.product_sale_date_label,
    required: Boolean = true,
    onDateChange: (Long) -> Unit = {},
) {
    var selectedDateMillis by remember { mutableStateOf(initialDateMillis) }
    DateTextFieldContent(
        dateMillis = selectedDateMillis,
        onDateChange = {
            selectedDateMillis = it
            onDateChange(it)
        },
        modifier = modifier,
        labelResId = labelResId,
        required = required
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTextFieldContent(
    dateMillis: Long?,
    onDateChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    @StringRes labelResId: Int = R.string.product_sale_date_label,
    required: Boolean = true,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(8.dp)
    val label = stringResource(labelResId)

    Column(modifier = modifier.fillMaxWidth()) {
        Row {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = DarkPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(
                    if (required) R.string.field_required_marker
                    else R.string.field_optional_marker
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = if (required) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(MaterialTheme.colorScheme.surface, shape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                .clickable { showDatePicker = true }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = modifier.clickable { showDatePicker = true },
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = DarkPrimaryContainer
            )
            Text(
                text = dateMillis?.let(::formatSaleDatePtBr)
                    ?: stringResource(R.string.product_sale_date_placeholder),
                color = if (dateMillis == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            )
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = stringResource(R.string.date_select_description),
                tint = DarkPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = dateMillis ?: todayUtcMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let(onDateChange)
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(R.string.date_picker_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.date_picker_cancel))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

internal fun formatSaleDatePtBr(dateMillis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("pt-BR")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(dateMillis))

private fun todayUtcMillis(): Long =
    Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

@Preview(showBackground = true, name = "Data da venda")
@Composable
private fun DateTextFieldPreview() {
    LucroVarejoTheme {
        DateTextField()
    }
}
