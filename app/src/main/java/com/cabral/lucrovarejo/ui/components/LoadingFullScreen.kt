package com.cabral.lucrovarejo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun LoadingFullScreen(
    modifier: Modifier = Modifier,
    @StringRes loadingTextId: Int,
) {
    LoadingFullScreenContent(
        modifier = modifier,
        loadingTextId = loadingTextId
    )
}

@Composable
fun LoadingFullScreenContent(
    modifier: Modifier = Modifier,
    @StringRes loadingTextId: Int,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator()
            Text(text = stringResource(loadingTextId))
        }
    }
}

@Preview
@Composable
fun LoadingFullScreenPreview() {
    LucroVarejoTheme {
        LoadingFullScreen(loadingTextId = R.string.purchase_load_products_loading)
    }
}