package com.cabral.lucrovarejo.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cabral.lucrovarejo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggedRouteTopBar(
    route: String,
    onBackPress: () -> Unit,
) {
    val titleId = when (route) {
        LoggedRoutes.HOME -> R.string.home_header_title
        LoggedRoutes.PRODUCTS -> R.string.products_title
        LoggedRoutes.ADD_PRODUCT -> R.string.new_product
        LoggedRoutes.EDIT_PRODUCT -> R.string.edit_product
        LoggedRoutes.SALES -> R.string.sales_title
        LoggedRoutes.SUMMARY -> R.string.summary_title
        LoggedRoutes.CONFIG -> R.string.config_title
        LoggedRoutes.ADD_PURCHASE -> R.string.add_purchase_title
        LoggedRoutes.EDIT_SALES -> R.string.edit_purchase_title
        LoggedRoutes.MACHINE_LIST -> R.string.terminals_title
        LoggedRoutes.ADD_MACHINE -> R.string.terminal_form_add_title
        LoggedRoutes.EDIT_MACHINE -> R.string.terminal_form_edit_title
        LoggedRoutes.MACHINE_HISTORY -> R.string.machine_history_title
        else -> return
    }
    TopAppBar(
        title = {
            Text(
                text = stringResource(titleId),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        navigationIcon = {
            if (showBackButton(route)) {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    )
}

private fun showBackButton(route: String): Boolean {
    return route == LoggedRoutes.ADD_PRODUCT ||
            route == LoggedRoutes.EDIT_PRODUCT ||
            route == LoggedRoutes.EDIT_SALES ||
            route == LoggedRoutes.ADD_PURCHASE ||
            route == LoggedRoutes.MACHINE_LIST ||
            route == LoggedRoutes.ADD_MACHINE ||
            route == LoggedRoutes.EDIT_MACHINE ||
            route == LoggedRoutes.MACHINE_HISTORY
}
