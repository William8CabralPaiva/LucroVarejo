package com.cabral.lucrovarejo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun PasswordTextFieldStateful(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelResId: Int,
    @StringRes showPasswordDescriptionResId: Int,
    @StringRes hidePasswordDescriptionResId: Int,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    PasswordTextFieldStateless(
        value = value,
        onValueChange = onValueChange,
        labelResId = labelResId,
        showPasswordDescriptionResId = showPasswordDescriptionResId,
        hidePasswordDescriptionResId = hidePasswordDescriptionResId,
        passwordVisible = passwordVisible,
        onPasswordVisibilityChange = { passwordVisible = it },
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText
    )
}

@Composable
fun PasswordTextFieldStateless(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes labelResId: Int,
    @StringRes showPasswordDescriptionResId: Int,
    @StringRes hidePasswordDescriptionResId: Int,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelResId)) },
        supportingText = supportingText,
        isError = isError,
        singleLine = true,
        enabled = enabled,
        visualTransformation = passwordVisualTransformation(passwordVisible),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Filled.VisibilityOff
                    } else {
                        Icons.Filled.Visibility
                    },
                    contentDescription = stringResource(
                        if (passwordVisible) {
                            hidePasswordDescriptionResId
                        } else {
                            showPasswordDescriptionResId
                        }
                    )
                )
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

internal fun passwordVisualTransformation(passwordVisible: Boolean): VisualTransformation =
    if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()

@Preview(showBackground = true, name = "Senha stateful")
@Composable
private fun PasswordTextFieldStatefulPreview() {
    LucroVarejoTheme {
        PasswordTextFieldStateful(
            value = "segura123",
            onValueChange = {},
            labelResId = R.string.register_password_label,
            showPasswordDescriptionResId = R.string.register_password_show,
            hidePasswordDescriptionResId = R.string.register_password_hide,
        )
    }
}

@Preview(showBackground = true, name = "Senha stateless")
@Composable
private fun PasswordTextFieldStatelessPreview() {
    LucroVarejoTheme {
        PasswordTextFieldStateless(
            value = "segura123",
            onValueChange = {},
            labelResId = R.string.register_password_label,
            showPasswordDescriptionResId = R.string.register_password_show,
            hidePasswordDescriptionResId = R.string.register_password_hide,
            passwordVisible = false,
            onPasswordVisibilityChange = {}
        )
    }
}
