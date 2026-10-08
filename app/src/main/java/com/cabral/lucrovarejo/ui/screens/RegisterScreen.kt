package com.cabral.lucrovarejo.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.R

@Composable
fun RegisterScreen(
    onBackPress: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

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
        onBackPress = onBackPress,
        passwordVisible = passwordVisible,
        confirmPasswordVisible = confirmPasswordVisible,
        onPasswordVisibilityChange = { passwordVisible = it },
        onConfirmPasswordVisibilityChange = { confirmPasswordVisible = it }
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
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    onConfirmPasswordVisibilityChange: (Boolean) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
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
                color = MaterialTheme.colorScheme.primary
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
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_field_spacing)))

            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.register_password_label)) },
                supportingText = {
                    state.passwordError?.let { Text(stringResource(it)) }
                },
                isError = state.passwordError != null,
                singleLine = true,
                enabled = !state.isLoading,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                        Icon(
                            imageVector = if (passwordVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (passwordVisible) {
                                stringResource(R.string.register_password_hide)
                            } else {
                                stringResource(R.string.register_password_show)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_field_spacing)))

            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                label = { Text(stringResource(R.string.register_confirm_password_label)) },
                supportingText = {
                    state.confirmPasswordError?.let { Text(stringResource(it)) }
                },
                isError = state.confirmPasswordError != null,
                singleLine = true,
                enabled = !state.isLoading,
                visualTransformation = if (confirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            onConfirmPasswordVisibilityChange(!confirmPasswordVisible)
                        }
                    ) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) {
                                Icons.Filled.VisibilityOff
                            } else {
                                Icons.Filled.Visibility
                            },
                            contentDescription = if (confirmPasswordVisible) {
                                stringResource(R.string.register_confirm_password_hide)
                            } else {
                                stringResource(R.string.register_confirm_password_show)
                            }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_field_spacing)))
                Text(stringResource(it), color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_section_spacing)))

            Button(
                onClick = onSubmit,
                enabled = state.isFormValid && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(stringResource(R.string.register_submit))
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.register_field_spacing)))

            TextButton(
                onClick = onBackPress,
                enabled = !state.isLoading
            ) {
                Text(stringResource(R.string.register_existing_account))
            }
        }
    }
}

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
            onBackPress = {},
            passwordVisible = false,
            confirmPasswordVisible = false,
            onPasswordVisibilityChange = {},
            onConfirmPasswordVisibilityChange = {}
        )
    }
}
