package com.cabral.lucrovarejo.data.store

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

fun interface ActiveStoreKeyProvider {
    fun currentStoreKey(): String?
}

// Loja ativa do usuario logado. Sem escolha salva, usa o proprio UID (contas anteriores a multi-loja).
@Singleton
class ActiveStoreHolder @Inject constructor(
    private val auth: FirebaseAuth,
    @ApplicationContext context: Context
) : ActiveStoreKeyProvider {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
    private var cached: Pair<String, String>? = null

    @Synchronized
    override fun currentStoreKey(): String? {
        val uid = auth.currentUser?.uid ?: return null
        cached?.takeIf { it.first == uid }?.let { return it.second }
        val storeKey = preferences.getString(preferenceKey(uid), null) ?: uid
        cached = uid to storeKey
        return storeKey
    }

    @Synchronized
    fun select(storeKey: String) {
        val uid = auth.currentUser?.uid ?: return
        cached = uid to storeKey
        preferences.edit { putString(preferenceKey(uid), storeKey) }
    }

    private fun preferenceKey(uid: String) = "$KEY_PREFIX$uid"

    private companion object {
        const val PREFERENCES = "lucro_varejo_active_store"
        const val KEY_PREFIX = "active_store_"
    }
}
