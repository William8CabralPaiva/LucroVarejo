package com.cabral.lucrovarejo.domain.auth

sealed interface GoogleSignInResult {
    data object Success : GoogleSignInResult

    // Conta Google nova: autenticada, mas a loja ainda precisa de um nome para ser criada no Firestore.
    data object NeedsStoreName : GoogleSignInResult
}

interface GoogleAuthRepository {
    // Se a loja ainda nao existe e displayName for nulo, retorna NeedsStoreName.
    suspend fun signIn(idToken: String, displayName: String?): GoogleSignInResult

    suspend fun completeRegistration(displayName: String)

    fun cancelPendingRegistration()
}
