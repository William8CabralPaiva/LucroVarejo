package com.cabral.lucrovarejo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SalesScreen() {
    TabPlaceholderScreen(
        title = "Vendas",
        message = "Suas vendas aparecerão aqui."
    )
}

@Composable
fun SummaryScreen() {
    TabPlaceholderScreen(
        title = "Resumo",
        message = "O resumo das vendas aparecerá aqui."
    )
}

@Composable
private fun TabPlaceholderScreen(
    title: String,
    message: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
