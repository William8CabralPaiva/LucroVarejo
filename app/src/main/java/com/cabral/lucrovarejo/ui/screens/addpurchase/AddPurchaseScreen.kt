package com.cabral.lucrovarejo.ui.screens.addpurchase

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.components.Spinner
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun AddPurchaseScreen() {
    AddPurchaseScreenContent()
}

@Composable
fun AddPurchaseScreenContent() {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.register_screen_padding)),
        ) {
            Spinner(
                textId = R.string.product,
                placeholderResId = R.string.product_placeholder,
                items = emptyList()
            )
            Spacer(modifier = Modifier.height(4.dp))

        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddPurchaseScreenPreview() {
    LucroVarejoTheme {
        AddPurchaseScreen()
    }
}