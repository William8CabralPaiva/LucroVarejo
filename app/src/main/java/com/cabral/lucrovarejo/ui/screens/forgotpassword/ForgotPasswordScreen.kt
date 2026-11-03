package com.cabral.lucrovarejo.ui.screens.forgotpassword

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.components.LoadingButton

@Composable
fun ForgotPasswordScreen(
    onBackPress: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    ForgotPasswordScreenContent(
        state = state,
        onStoreNameChange = viewModel::onStoreNameChanged,
        onEmailChange = viewModel::onEmailChanged,
        onSubmit = viewModel::submit,
        onBackPress = onBackPress
    )
}

@Composable
internal fun ForgotPasswordScreenContent(
    state: ForgotPasswordUiState,
    onStoreNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBackPress: () -> Unit,
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
                text = "Esqueci a senha",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Informe seu usuário e o e-mail cadastrado na loja. " +
                    "Se os dados conferirem, enviaremos um link para redefinir a senha.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = state.storeName,
                onValueChange = onStoreNameChange,
                label = { Text("Usuário") },
                singleLine = true,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(FORGOT_STORE_NAME_FIELD_TAG)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.register_email_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(FORGOT_EMAIL_FIELD_TAG)
            )

            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            if (state.isRequestSent) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Se os dados estiverem corretos, você receberá um e-mail " +
                        "com o link para redefinir a senha.",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.testTag(FORGOT_SUCCESS_TAG)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            LoadingButton(
                onClick = onSubmit,
                enabled = state.storeName.isNotBlank() && state.email.isNotBlank(),
                isLoading = state.isLoading,
                textId = R.string.forgot_password_submit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onBackPress,
                modifier = Modifier.testTag(FORGOT_BACK_BUTTON_TAG)
            ) {
                Text("Voltar ao login")
            }
        }
    }
}

internal const val FORGOT_STORE_NAME_FIELD_TAG = "forgot-store-name-field"
internal const val FORGOT_EMAIL_FIELD_TAG = "forgot-email-field"
internal const val FORGOT_SUCCESS_TAG = "forgot-success"
internal const val FORGOT_BACK_BUTTON_TAG = "forgot-back-button"
