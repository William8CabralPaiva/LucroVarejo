package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.ThemeMode
import java.text.DateFormatSymbols
import java.util.Locale

@Composable
fun MonthYearSelector(
    year: Int,
    month: Int,
    onMonthYearChange: (year: Int, month: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val invalidYearMessage = stringResource(R.string.month_year_invalid_year)
    val invalidMonthMessage = stringResource(R.string.month_year_invalid_month)
    require(year > 0) { invalidYearMessage }
    require(month in 1..12) { invalidMonthMessage }
    val locale = Locale.Builder().setLanguage("pt").setRegion("BR").build()
    val monthName = DateFormatSymbols.getInstance(locale).months[month - 1].replaceFirstChar {
        it.titlecase(locale)
    }
    val label = "$monthName $year"
    val shape = RoundedCornerShape(dimensionResource(R.dimen.month_year_selector_radius))
    val previousDescription = stringResource(R.string.month_year_previous)
    val nextDescription = stringResource(R.string.month_year_next)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.month_year_selector_height))
            .border(
                width = dimensionResource(R.dimen.month_year_selector_border_width),
                color = MaterialTheme.colorScheme.outline,
                shape = shape
            ),
        shape = shape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = dimensionResource(R.dimen.month_year_selector_padding)
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = stringResource(R.string.month_year_calendar_description),
                modifier = Modifier.size(dimensionResource(R.dimen.month_year_selector_icon_size)),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = dimensionResource(R.dimen.month_year_selector_padding)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            IconButton(
                onClick = {
                    if (month == 1) {
                        onMonthYearChange(year - 1, 12)
                    } else {
                        onMonthYearChange(year, month - 1)
                    }
                },
                modifier = Modifier.semantics {
                    contentDescription = previousDescription
                }) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft, contentDescription = null
                )
            }
            IconButton(
                onClick = {
                    if (month == 12) {
                        onMonthYearChange(year + 1, 1)
                    } else {
                        onMonthYearChange(year, month + 1)
                    }
                },
                modifier = Modifier.semantics {
                    contentDescription = nextDescription
                }) {
                Icon(
                    imageVector = Icons.Default.ChevronRight, contentDescription = null
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Seletor de mês - claro")
@Composable
private fun MonthYearSelectorLightPreview() {
    LucroVarejoTheme(themeMode = ThemeMode.LIGHT) {
        var year by remember { mutableStateOf(2026) }
        var month by remember { mutableStateOf(10) }
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            MonthYearSelector(
                year = year,
                month = month,
                onMonthYearChange = { newYear, newMonth ->
                    year = newYear
                    month = newMonth
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "Seletor de mês - escuro")
@Composable
private fun MonthYearSelectorDarkPreview() {
    LucroVarejoTheme(themeMode = ThemeMode.DARK) {
        var year by remember { mutableStateOf(2026) }
        var month by remember { mutableStateOf(10) }
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            MonthYearSelector(
                year = year,
                month = month,
                onMonthYearChange = { newYear, newMonth ->
                    year = newYear
                    month = newMonth
                }
            )
        }
    }
}
