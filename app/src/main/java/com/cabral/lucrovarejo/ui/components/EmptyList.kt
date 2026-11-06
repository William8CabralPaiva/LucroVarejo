package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.ui.theme.ThemeMode
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun EmptyList(
    onAddPurchase: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.alert_no_products),
    message: String = stringResource(R.string.purchase_area),
    actionText: String = stringResource(R.string.empty_list_add_purchase),
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_clipboard),
            contentDescription = title,
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = message,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(8.dp))
        IconButton(
            enabled = true,
            onClick = onAddPurchase,
            icon = Icons.Filled.AddCircle,
            iconContentDescription = actionText,
            text = actionText
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyListPreview() {
    LucroVarejoTheme {
        EmptyList(onAddPurchase = {})
    }
}

@Preview(showBackground = true, name = "Lista vazia - produtos")
@Composable
private fun EmptyListProductsPreview() {
    LucroVarejoTheme {
        EmptyList(
            onAddPurchase = {},
            title = stringResource(R.string.products_empty_title),
            message = stringResource(R.string.products_empty_message),
            actionText = stringResource(R.string.products_add)
        )
    }
}

@Preview(showBackground = true, name = "Lista vazia - maquininhas")
@Composable
private fun EmptyListTerminalsPreview() {
    LucroVarejoTheme {
        EmptyList(
            onAddPurchase = {},
            title = stringResource(R.string.terminals_empty),
            message = stringResource(R.string.terminals_empty_message),
            actionText = stringResource(R.string.terminals_add)
        )
    }
}

@Preview(showBackground = true, name = "Lista vazia - escuro")
@Composable
private fun EmptyListDarkPreview() {
    LucroVarejoTheme(themeMode = ThemeMode.DARK) {
        EmptyList(onAddPurchase = {})
    }
}
