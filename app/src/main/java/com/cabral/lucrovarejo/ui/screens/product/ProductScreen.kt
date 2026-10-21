package com.cabral.lucrovarejo.ui.screens.product

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.ui.components.ProdCodPrice
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.math.BigDecimal

@Composable
fun ProductScreen(
    onAddProduct: () -> Unit,
    onEditProduct: () -> Unit = {},
    viewModel: ProductViewModel? = null,
) {
    val resolvedViewModel = viewModel ?: hiltViewModel()
    val state by resolvedViewModel.uiState.collectAsState()
    ProductContent(
        state = state,
        onQueryChange = resolvedViewModel::onQueryChange,
        onAddProduct = onAddProduct,
        onEditProduct = onEditProduct
    )
}

@Composable
fun ProductContent(
    state: ProductUiState,
    onQueryChange: (String) -> Unit,
    onAddProduct: () -> Unit,
    onEditProduct: () -> Unit = {},
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddProduct,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.products_add)
                )
            }
        }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            state.hasError -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.products_load_error),
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillParentMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                        },
                        placeholder = {
                            Text(stringResource(R.string.products_search_placeholder))
                        }
                    )
                }
                if (state.items.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.products_empty),
                            modifier = Modifier.padding(top = 24.dp)
                        )
                    }
                } else {
                    items(state.items, key = { it.key ?: "${it.cod}-${it.name}" }) { product ->
                        ProdCodPrice(
                            onClick = onEditProduct,
                            name = product.name,
                            code = product.cod,
                            cost = product.cost
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Produtos")
@Composable
private fun ProductContentPreview() {
    LucroVarejoTheme {
        ProductContent(
            state = ProductUiState(
                isLoading = false,
                items = listOf(
                    ProductModel(
                        key = "1",
                        cod = "7891000123456",
                        name = "Bolsa Feminina",
                        cost = BigDecimal("89.90"),
                        description = ""
                    )
                )
            ),
            onQueryChange = {},
            onAddProduct = {},
            onEditProduct = {}
        )
    }
}
