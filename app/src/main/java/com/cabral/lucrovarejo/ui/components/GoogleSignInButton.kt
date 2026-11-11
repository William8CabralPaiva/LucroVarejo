package com.cabral.lucrovarejo.ui.components

import android.content.Context
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.cabral.lucrovarejo.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

internal class GoogleSignInException(cause: Throwable? = null) : Exception(cause)

// Abre o seletor de contas Google e devolve o ID token. Retorna null se o usuario cancelar.
internal suspend fun fetchGoogleIdToken(context: Context): String? {
    // default_web_client_id so existe depois que o google-services.json tem o cliente OAuth web.
    val webClientId = context.resources
        .getIdentifier("default_web_client_id", "string", context.packageName)
        .takeIf { it != 0 }
        ?.let(context::getString)
        ?: throw GoogleSignInException()
    val request = GetCredentialRequest.Builder()
        .addCredentialOption(GetSignInWithGoogleOption.Builder(webClientId).build())
        .build()
    return try {
        val credential = CredentialManager.create(context).getCredential(context, request).credential
        if (credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        } else {
            throw GoogleSignInException()
        }
    } catch (_: GetCredentialCancellationException) {
        null
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: GoogleSignInException) {
        throw exception
    } catch (exception: Exception) {
        throw GoogleSignInException(exception)
    }
}

@Composable
fun GoogleSignInButton(
    onIdToken: (String) -> Unit,
    onError: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    textId: Int = R.string.google_sign_in,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnIdToken by rememberUpdatedState(onIdToken)
    val currentOnError by rememberUpdatedState(onError)

    OutlinedButton(
        onClick = {
            scope.launch {
                try {
                    fetchGoogleIdToken(context)?.let(currentOnIdToken)
                } catch (_: GoogleSignInException) {
                    currentOnError()
                }
            }
        },
        enabled = enabled,
        modifier = modifier
    ) {
        Text(stringResource(textId))
    }
}
