package com.cabral.lucrovarejo.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.ThemeMode

private const val DETAILS_SEPARATOR = "  •  "

/** Item de produto (nome, código e custo), usado na lista de sugestões. */
@Composable
fun ProductOptionItem(
    name: String,
    code: String,
    formattedCost: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = name,
            textAlign = TextAlign.Unspecified,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        ProductOptionDetails(code = code, formattedCost = formattedCost)
    }
}

/** Linha de detalhes do produto: código (se houver) e custo. */
@Composable
fun ProductOptionDetails(
    code: String,
    formattedCost: String,
    modifier: Modifier = Modifier,
) {
    val details = buildList {
        if (code.isNotBlank()) {
            add(stringResource(R.string.product_code_value, code))
        }
        add(stringResource(R.string.product_cost_value, formattedCost))
    }.joinToString(DETAILS_SEPARATOR)

    Text(
        text = details,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "Item completo")
@Composable
private fun ProductOptionItemPreview() {
    LucroVarejoTheme {
        ProductOptionItem(
            name = "Guarda-chuva",
            code = "001",
            formattedCost = "R$ 34,00",
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_16dp))
        )
    }
}

@Preview(showBackground = true, name = "Item sem código")
@Composable
private fun ProductOptionItemWithoutCodePreview() {
    LucroVarejoTheme {
        ProductOptionItem(
            name = "Guarda-chuva",
            code = "",
            formattedCost = "R$ 34,00",
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_16dp))
        )
    }
}

@Preview(showBackground = true, widthDp = 220, name = "Nome longo")
@Composable
private fun ProductOptionItemLongNamePreview() {
    LucroVarejoTheme {
        ProductOptionItem(
            name = "Guarda-chuva automático reforçado com cabo emborrachado",
            code = "GC-2024-001",
            formattedCost = "R$ 1.234,56",
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_16dp))
        )
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "Item no tema escuro"
)
@Composable
private fun ProductOptionItemDarkPreview() {
    LucroVarejoTheme(themeMode = ThemeMode.DARK) {
        Surface {
            ProductOptionItem(
                name = "Guarda-chuva",
                code = "001",
                formattedCost = "R$ 34,00",
                modifier = Modifier.padding(dimensionResource(R.dimen.spacing_16dp))
            )
        }
    }
}

@Preview(showBackground = true, name = "Somente detalhes")
@Composable
private fun ProductOptionDetailsPreview() {
    LucroVarejoTheme {
        ProductOptionDetails(
            code = "001",
            formattedCost = "R$ 34,00",
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_16dp))
        )
    }
}
