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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.components.AppKeyboardDefaults
import com.cabral.lucrovarejo.ui.components.AppOutlinedTextField
import com.cabral.lucrovarejo.ui.components.LoadingButton
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    ForgotPasswordScreenContent(
        state = state,
        onEmailChange = viewModel::onEmailChanged,
        onSubmit = viewModel::submit,
        onBack = onBack
    )
}

@Composable
internal fun ForgotPasswordScreenContent(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.spacing_24dp)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.forgot_password_title),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_8dp)))

            Text(
                stringResource(R.string.forgot_password_description),
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_24dp)))

            AppOutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.login_email_label)) },
                singleLine = true,
                enabled = !state.isLoading,
                keyboardOptions = AppKeyboardDefaults.email,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(FORGOT_PASSWORD_EMAIL_FIELD_TAG)
            )

            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_12dp)))
                Text(stringResource(it), color = MaterialTheme.colorScheme.error)
            }

            if (state.isEmailSent) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_12dp)))
                Text(
                    stringResource(R.string.forgot_password_success),
                    modifier = Modifier.testTag(FORGOT_PASSWORD_SUCCESS_TAG)
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_24dp)))

            LoadingButton(
                onClick = onSubmit,
                enabled = state.email.isNotBlank(),
                isLoading = state.isLoading,
                textId = R.string.forgot_password_submit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_16dp)))

            TextButton(onClick = onBack) {
                Text(stringResource(R.string.navigate_back))
            }
        }
    }
}

internal const val FORGOT_PASSWORD_EMAIL_FIELD_TAG = "forgot-password-email-field"
internal const val FORGOT_PASSWORD_SUCCESS_TAG = "forgot-password-success"

@Preview(showBackground = true, name = "Esqueci a senha")
@Composable
private fun ForgotPasswordScreenPreview() {
    LucroVarejoTheme {
        ForgotPasswordScreenContent(
            state = ForgotPasswordUiState(email = "contato@exemplo.com", isEmailSent = true),
            onEmailChange = {},
            onSubmit = {},
            onBack = {}
        )
    }
}
