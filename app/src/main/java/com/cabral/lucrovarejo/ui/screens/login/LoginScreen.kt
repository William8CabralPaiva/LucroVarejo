package com.cabral.lucrovarejo.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.components.AppKeyboardDefaults
import com.cabral.lucrovarejo.ui.components.AppOutlinedTextField
import com.cabral.lucrovarejo.ui.components.GoogleSignInButton
import com.cabral.lucrovarejo.ui.components.LoadingButton
import com.cabral.lucrovarejo.ui.components.PasswordTextField
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun LoginScreen(
    goToRegisterScreen: () -> Unit,
    goToLoggedFlow: () -> Unit,
    goToForgotPassword: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    val currentGoToLoggedFlow by rememberUpdatedState(goToLoggedFlow)
    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) currentGoToLoggedFlow()
    }

    LoginScreenContent(
        state = state,
        onEmailChange = viewModel::onEmailChanged,
        onPasswordChange = viewModel::onPasswordChanged,
        onSubmit = viewModel::signIn,
        onRegister = goToRegisterScreen,
        onForgotPassword = goToForgotPassword,
        onGoogleIdToken = viewModel::onGoogleIdToken,
        onGoogleError = viewModel::onGoogleFailure,
        onStoreNameChange = viewModel::onStoreNameChanged,
        onStoreNameConfirm = viewModel::confirmStoreName,
        onStoreNameDismiss = viewModel::dismissStoreName
    )
}

@Composable
internal fun LoginScreenContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit = {},
    onGoogleIdToken: (String) -> Unit = {},
    onGoogleError: () -> Unit = {},
    onStoreNameChange: (String) -> Unit = {},
    onStoreNameConfirm: () -> Unit = {},
    onStoreNameDismiss: () -> Unit = {},
) {
    if (state.needsStoreName) {
        StoreNameDialog(
            state = state,
            onNameChange = onStoreNameChange,
            onConfirm = onStoreNameConfirm,
            onDismiss = onStoreNameDismiss
        )
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.spacing_24dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_varejo),
                contentDescription = "Logo do app",
                modifier = Modifier.size(dimensionResource(R.dimen.size_160dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_32dp)))

            AppOutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.login_email_label)) },
                singleLine = true,
                enabled = !state.isLoading,
                keyboardOptions = AppKeyboardDefaults.email,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LOGIN_EMAIL_FIELD_TAG)
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_16dp)))

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
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_12dp)))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_24dp)))

            LoadingButton(
                onClick = onSubmit,
                enabled = state.email.isNotBlank() &&
                        state.password.isNotBlank(),
                isLoading = state.isLoading,
                textId = R.string.login_submit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_12dp)))

            GoogleSignInButton(
                onIdToken = onGoogleIdToken,
                onError = onGoogleError,
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(LOGIN_GOOGLE_BUTTON_TAG)
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_16dp)))

            TextButton(
                onClick = onForgotPassword,
                modifier = Modifier.testTag(LOGIN_FORGOT_PASSWORD_BUTTON_TAG)
            ) {
                Text(stringResource(R.string.login_forgot_password))
            }

            TextButton(
                onClick = onRegister,
                modifier = Modifier.testTag(LOGIN_REGISTER_BUTTON_TAG)
            ) {
                Text("Criar uma conta")
            }
        }
    }
}

internal const val LOGIN_EMAIL_FIELD_TAG = "login-email-field"
internal const val LOGIN_PASSWORD_FIELD_TAG = "login-password-field"
internal const val LOGIN_REGISTER_BUTTON_TAG = "login-register-button"
internal const val LOGIN_FORGOT_PASSWORD_BUTTON_TAG = "login-forgot-password-button"
internal const val LOGIN_GOOGLE_BUTTON_TAG = "login-google-button"
internal const val LOGIN_STORE_NAME_FIELD_TAG = "login-store-name-field"

@Composable
private fun StoreNameDialog(
    state: LoginUiState,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!state.isLoading) onDismiss() },
        title = { Text(stringResource(R.string.google_store_name_title)) },
        text = {
            Column {
                Text(stringResource(R.string.google_store_name_message))
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_12dp)))
                AppOutlinedTextField(
                    value = state.storeName,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.register_display_name_label)) },
                    supportingText = {
                        state.storeNameError?.let { Text(stringResource(it)) }
                    },
                    isError = state.storeNameError != null,
                    singleLine = true,
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(LOGIN_STORE_NAME_FIELD_TAG)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !state.isLoading && state.storeName.isNotBlank()
            ) {
                Text(stringResource(R.string.google_store_name_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.isLoading) {
                Text(stringResource(R.string.google_store_name_cancel))
            }
        }
    )
}

@Preview(showBackground = true, name = "Login")
@Composable
private fun LoginScreenPreview() {
    LucroVarejoTheme {
        LoginScreenContent(
            state = LoginUiState(email = "contato@exemplo.com", password = "segura123"),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onRegister = {},
            onForgotPassword = {}
        )
    }
}
