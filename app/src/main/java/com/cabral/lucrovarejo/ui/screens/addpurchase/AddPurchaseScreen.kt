package com.cabral.lucrovarejo.ui.screens.addpurchase

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
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