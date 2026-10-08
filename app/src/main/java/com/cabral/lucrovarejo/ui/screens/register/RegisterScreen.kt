package com.cabral.lucrovarejo.ui.screens.register

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.components.LoadingButton
import com.cabral.lucrovarejo.ui.components.PasswordTextFieldStateful
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun RegisterScreen(
    onBackPress: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isRegistered) {
        if (state.isRegistered) onRegisterSuccess()
    }

    RegisterScreenContent(
        state = state,
        onStoreNameChange = viewModel::onStoreNameChanged,
        onEmailChange = viewModel::onEmailChanged,
        onPasswordChange = viewModel::onPasswordChanged,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChanged,
        onSubmit = viewModel::register,
        onBackPress = onBackPress
    )
}

@Composable
internal fun RegisterScreenContent(
    state: RegisterUiState,
    onStoreNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.register_screen_padding)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = stringResource(R.string.register_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag(REGISTER_TITLE_TAG)
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_title_spacing)))

            OutlinedTextField(
                value = state.storeName,
                onValueChange = onStoreNameChange,
                label = { Text(stringResource(R.string.register_store_label)) },
                supportingText = {
                    Text(
                        state.storeNameError?.let { stringResource(it) }
                            ?: stringResource(R.string.register_store_hint)
                    )
                },
                isError = state.storeNameError != null,
                singleLine = true,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(REGISTER_STORE_NAME_FIELD_TAG)
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_field_spacing)))

            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.register_email_label)) },
                supportingText = {
                    state.emailError?.let { Text(stringResource(it)) }
                },
                isError = state.emailError != null,
                singleLine = true,
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(REGISTER_EMAIL_FIELD_TAG)
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_field_spacing)))

            PasswordTextFieldStateful(
                value = state.password,
                onValueChange = onPasswordChange,
                labelResId = R.string.register_password_label,
                showPasswordDescriptionResId = R.string.register_password_show,
                hidePasswordDescriptionResId = R.string.register_password_hide,
                supportingText = {
                    state.passwordError?.let { Text(stringResource(it)) }
                },
                isError = state.passwordError != null,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(REGISTER_PASSWORD_FIELD_TAG)
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_field_spacing)))

            PasswordTextFieldStateful(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                labelResId = R.string.register_confirm_password_label,
                showPasswordDescriptionResId = R.string.register_confirm_password_show,
                hidePasswordDescriptionResId = R.string.register_confirm_password_hide,
                supportingText = {
                    state.confirmPasswordError?.let { Text(stringResource(it)) }
                },
                isError = state.confirmPasswordError != null,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(REGISTER_CONFIRM_PASSWORD_FIELD_TAG)
            )

            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_field_spacing)))
                Text(stringResource(it), color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_section_spacing)))

            LoadingButton(
                textId = R.string.register_submit,
                enabled = state.isFormValid,
                isLoading = state.isLoading,
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_field_spacing)))

            TextButton(
                onClick = onBackPress,
                enabled = !state.isLoading,
                modifier = Modifier.testTag(REGISTER_BACK_BUTTON_TAG)
            ) {
                Text(stringResource(R.string.register_existing_account))
            }
        }
    }
}

internal const val REGISTER_TITLE_TAG = "register-title"
internal const val REGISTER_STORE_NAME_FIELD_TAG = "register-store-name-field"
internal const val REGISTER_EMAIL_FIELD_TAG = "register-email-field"
internal const val REGISTER_PASSWORD_FIELD_TAG = "register-password-field"
internal const val REGISTER_CONFIRM_PASSWORD_FIELD_TAG = "register-confirm-password-field"
internal const val REGISTER_BACK_BUTTON_TAG = "register-back-button"

@Preview(showBackground = true, name = "Cadastro da loja")
@Composable
private fun RegisterScreenPreview() {
    LucroVarejoTheme {
        RegisterScreenContent(
            state = RegisterUiState(
                storeName = "lojaexemplo",
                email = "contato@exemplo.com",
                password = "segura123",
                confirmPassword = "segura123"
            ),
            onStoreNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onBackPress = {}
        )
    }
}
