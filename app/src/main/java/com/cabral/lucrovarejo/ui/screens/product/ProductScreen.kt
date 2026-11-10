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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.ui.theme.ThemeMode
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.product.ProductModel
import com.cabral.lucrovarejo.ui.components.AppOutlinedTextField
import com.cabral.lucrovarejo.ui.components.EmptyList
import com.cabral.lucrovarejo.ui.components.ProdCodPrice
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.math.BigDecimal

@Composable
fun ProductScreen(
    onAddProduct: () -> Unit,
    onEditProduct: (ProductModel) -> Unit = {},
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
    modifier: Modifier = Modifier,
    onEditProduct: (ProductModel) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
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
                    .padding(horizontal = dimensionResource(R.dimen.spacing_16dp)),
                contentPadding = PaddingValues(top = dimensionResource(R.dimen.product_list_top_padding), bottom = dimensionResource(R.dimen.size_88dp)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.product_list_item_spacing))
            ) {
                item {
                    AppOutlinedTextField(
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
                        if (state.query.isBlank()) {
                            EmptyList(
                                title = stringResource(R.string.products_empty_title),
                                message = stringResource(R.string.products_empty_message),
                                actionText = stringResource(R.string.products_add),
                                onAddPurchase = onAddProduct,
                                modifier = Modifier.fillParentMaxHeight(0.7f)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.products_empty),
                                modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_24dp))
                            )
                        }
                    }
                } else {
                    items(state.items, key = { it.key ?: "${it.cod}-${it.name}" }) { product ->
                        ProdCodPrice(
                            onClick = { onEditProduct(product) },
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

@Preview(showBackground = true, name = "Produtos vazio")
@Composable
private fun ProductContentEmptyPreview() {
    LucroVarejoTheme {
        ProductContent(
            state = ProductUiState(isLoading = false),
            onQueryChange = {},
            onAddProduct = {}
        )
    }
}

@Preview(showBackground = true, name = "Produtos sem resultado na busca")
@Composable
private fun ProductContentNoSearchResultPreview() {
    LucroVarejoTheme {
        ProductContent(
            state = ProductUiState(isLoading = false, query = "xyz"),
            onQueryChange = {},
            onAddProduct = {}
        )
    }
}

@Preview(showBackground = true, name = "Produtos com erro")
@Composable
private fun ProductContentErrorPreview() {
    LucroVarejoTheme {
        ProductContent(
            state = ProductUiState(isLoading = false, hasError = true),
            onQueryChange = {},
            onAddProduct = {}
        )
    }
}

@Preview(showBackground = true, name = "Produtos vazio - escuro")
@Composable
private fun ProductContentEmptyDarkPreview() {
    LucroVarejoTheme(themeMode = ThemeMode.DARK) {
        ProductContent(
            state = ProductUiState(isLoading = false),
            onQueryChange = {},
            onAddProduct = {}
        )
    }
}
