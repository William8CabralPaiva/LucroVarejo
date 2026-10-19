package com.cabral.lucrovarejo.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
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
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.components.IconButton
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun EmptyList(
    onAddPurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_clipboard),
            contentDescription = "Nenhuma venda registrada",
            modifier = Modifier.size(120.dp),
            contentScale = ContentScale.Fit
        )
        Text(
            text = stringResource(R.string.alert_no_products),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = stringResource(R.string.purchase_area),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.size(8.dp))
        val addPurchaseText = stringResource(R.string.empty_list_add_purchase)
        IconButton(
            enabled = true,
            onClick = onAddPurchase,
            icon = Icons.Filled.AddCircle,
            iconContentDescription = addPurchaseText,
            text = addPurchaseText
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyListPreview() {
    LucroVarejoTheme {
        EmptyList(onAddPurchase = {})
    }
}