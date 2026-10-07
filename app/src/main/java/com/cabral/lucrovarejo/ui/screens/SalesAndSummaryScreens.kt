package com.cabral.lucrovarejo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen() {
    TabPlaceholderScreen(
        title = "Vendas",
        message = "Suas vendas aparecerão aqui."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen() {
    TabPlaceholderScreen(
        title = "Resumo",
        message = "O resumo das vendas aparecerá aqui."
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabPlaceholderScreen(
    title: String,
    message: String
) {
    androidx.compose.material3.Scaffold(
        topBar = {
            TopAppBar(title = { Text(title) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
}
