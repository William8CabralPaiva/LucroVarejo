package com.cabral.lucrovarejo.ui.screens.product

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun ProductScreen() {
    ProductContent()
}

@Composable
fun ProductContent() {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProductScreenPreview() {
    LucroVarejoTheme {
        ProductScreen()
    }
}