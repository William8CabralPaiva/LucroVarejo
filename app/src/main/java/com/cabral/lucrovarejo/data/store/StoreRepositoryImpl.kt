package com.cabral.lucrovarejo.data.store

import com.cabral.lucrovarejo.data.firestore.FirestoreCollections
import com.cabral.lucrovarejo.domain.store.StoreRepository
import com.cabral.lucrovarejo.domain.store.StoreSelection
import com.cabral.lucrovarejo.domain.store.StoreSummary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

@Singleton
class StoreRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val activeStore: ActiveStoreHolder
) : StoreRepository {

    override suspend fun getStoreSelection(): StoreSelection {
        val uid = requireUid()
        val userStores = StoreFirestore.userStores(firestore, uid)
        val stores = userStores.get().await().documents
            .map { StoreSummary(it.id, it.getString(StoreFirestore.FIELD_DISPLAY_NAME).orEmpty()) }
            .toMutableList()

        // Conta anterior a multi-loja: a loja tem o UID como id e ainda nao tem atalho na lista.
        if (stores.none { it.id == uid }) {
            legacyStore(uid)?.let { legacy ->
                stores += legacy
                backfillLegacyStore(uid, legacy)
            }
        }

        return StoreSelection(
            stores = stores.sortedBy { it.displayName.lowercase() },
            activeStoreId = activeStore.currentStoreKey()
        )
    }

    override suspend fun createStore(displayName: String): StoreSummary {
        val user = auth.currentUser ?: error("No active session.")
        val name = displayName.trim()
        val storeId = firestore.collection(FirestoreCollections.STORES).document().id
        StoreFirestore.newStoreBatch(
            firestore = firestore,
            storeId = storeId,
            uid = user.uid,
            email = user.email.orEmpty(),
            displayName = name
        ).commit().await()
        return StoreSummary(storeId, name)
    }

    override fun selectStore(storeId: String) {
        activeStore.select(storeId)
    }

    private suspend fun legacyStore(uid: String): StoreSummary? {
        val snapshot = firestore.collection(FirestoreCollections.STORES)
            .document(uid)
            .get()
            .await()
        if (!snapshot.exists()) return null
        return StoreSummary(uid, snapshot.getString(StoreFirestore.FIELD_DISPLAY_NAME).orEmpty())
    }

    // Melhor esforco: se falhar, a loja continua acessivel e o atalho e recriado na proxima abertura.
    private suspend fun backfillLegacyStore(uid: String, store: StoreSummary) {
        try {
            StoreFirestore.userStores(firestore, uid)
                .document(store.id)
                .set(StoreFirestore.userStoreFields(store.displayName))
                .await()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
        }
    }

    private fun requireUid(): String = auth.currentUser?.uid ?: error("No active session.")
}
