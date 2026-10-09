package com.cabral.lucrovarejo.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.components.LoadingButton
import com.cabral.lucrovarejo.ui.components.PasswordTextField

@Composable
fun LoginScreen(
    goToRegisterScreen: () -> Unit,
    goToLoggedFlow: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) goToLoggedFlow()
    }

    LoginScreenContent(
        state = state,
        onStoreNameChange = viewModel::onStoreNameChanged,
        onPasswordChange = viewModel::onPasswordChanged,
        onSubmit = viewModel::signIn,
        onRegister = goToRegisterScreen
    )
}

@Composable
internal fun LoginScreenContent(
    state: LoginUiState,
    onStoreNameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRegister: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = state.storeName,
                onValueChange = onStoreNameChange,
                label = { Text("Loja") },
                singleLine = true,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LOGIN_STORE_NAME_FIELD_TAG)
            )

            Spacer(modifier = Modifier.height(16.dp))

            PasswordTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                labelResId = R.string.register_password_label,
                showPasswordDescriptionResId = R.string.register_password_show,
                hidePasswordDescriptionResId = R.string.register_password_hide,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LOGIN_PASSWORD_FIELD_TAG)
            )

            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(24.dp))

            LoadingButton(
                onClick = onSubmit,
                enabled = state.storeName.isNotBlank() &&
                        state.password.isNotBlank(),
                isLoading = state.isLoading,
                textId = R.string.login_submit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onRegister,
                modifier = Modifier.testTag(LOGIN_REGISTER_BUTTON_TAG)
            ) {
                Text("Criar uma conta")
            }
        }
    }
}

internal const val LOGIN_STORE_NAME_FIELD_TAG = "login-store-name-field"
internal const val LOGIN_PASSWORD_FIELD_TAG = "login-password-field"
internal const val LOGIN_REGISTER_BUTTON_TAG = "login-register-button"
