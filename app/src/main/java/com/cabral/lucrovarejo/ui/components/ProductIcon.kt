package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.text.Normalizer
import java.util.Locale

@Composable
fun ProductIcon(
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Icon(
        imageVector = productIconFor(text),
        contentDescription = null,
        modifier = modifier,
        tint = tint
    )
}

fun productIconFor(text: String): ImageVector {
    val normalized = Normalizer.normalize(text.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")
    return when {
        listOf("guarda chuva", "guarda-chuva", "guardachuva", "sombrinha", "pontaria")
            .any { normalized.contains(it) } -> UmbrellaIcon
        normalized.contains("mochila") -> BackpackIcon
        normalized.contains("bag") -> GymBagIcon
        normalized.contains("bolsa") -> Icons.Outlined.ShoppingBag
        normalized.contains("carteira") -> Icons.Outlined.AccountBalanceWallet
        normalized.contains("meia") -> SockIcon
        normalized.contains("chinelo") -> SlipperIcon
        listOf("sapato", "calcado", "tenis").any { normalized.contains(it) } -> SneakerIcon
        else -> Icons.Outlined.Inventory2
    }
}

@Composable
private fun ProductIconPreviewRow(text: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_12dp)),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        ProductIcon(text = text)
        Text(text = text)
    }
}

@Preview(showBackground = true, name = "Bolsa")
@Composable
private fun ProductIconBolsaPreview() {
    LucroVarejoTheme { Column(Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) { ProductIconPreviewRow("Bolsa") } }
}

@Preview(showBackground = true, name = "Mochila")
@Composable
private fun ProductIconMochilaPreview() {
    LucroVarejoTheme { Column(Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) { ProductIconPreviewRow("Mochila") } }
}

@Preview(showBackground = true, name = "Bag de academia")
@Composable
private fun ProductIconBagPreview() {
    LucroVarejoTheme { Column(Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) { ProductIconPreviewRow("Bag de academia") } }
}

@Preview(showBackground = true, name = "Carteira")
@Composable
private fun ProductIconCarteiraPreview() {
    LucroVarejoTheme { Column(Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) { ProductIconPreviewRow("Carteira") } }
}

@Preview(showBackground = true, name = "Meias")
@Composable
private fun ProductIconMeiasPreview() {
    LucroVarejoTheme { Column(Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) { ProductIconPreviewRow("Meias") } }
}

@Preview(showBackground = true, name = "Sapato")
@Composable
private fun ProductIconSapatoPreview() {
    LucroVarejoTheme { Column(Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) { ProductIconPreviewRow("Sapato") } }
}

@Preview(showBackground = true, name = "Tênis")
@Composable
private fun ProductIconTenisPreview() {
    LucroVarejoTheme { Column(Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) { ProductIconPreviewRow("Tênis") } }
}

@Preview(showBackground = true, name = "Chinelo")
@Composable
private fun ProductIconChineloPreview() {
    LucroVarejoTheme { Column(Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) { ProductIconPreviewRow("Chinelo") } }
}

@Preview(showBackground = true, name = "Guarda-chuva")
@Composable
private fun ProductIconGuardaChuvaPreview() {
    LucroVarejoTheme { Column(Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) { ProductIconPreviewRow("Guarda-chuva") } }
}

@Preview(showBackground = true, name = "Genérico")
@Composable
private fun ProductIconGenericoPreview() {
    LucroVarejoTheme { Column(Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) { ProductIconPreviewRow("Camiseta") } }
}



