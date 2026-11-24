package com.cabral.lucrovarejo.data.store

import com.cabral.lucrovarejo.data.firestore.FirestoreCollections
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch

internal object StoreFirestore {
    const val FIELD_EMAIL = "email"
    const val FIELD_DISPLAY_NAME = "displayName"
    const val FIELD_CREATED_AT = "createdAt"
    const val FIELD_ROLE = "role"
    const val ROLE_OWNER = "OWNER"

    fun userStores(firestore: FirebaseFirestore, uid: String) =
        firestore.collection(FirestoreCollections.USERS).document(uid).collection(FirestoreCollections.USER_STORES)

    // Loja + membro dono + atalho na lista de lojas do usuario, gravados de forma atomica.
    fun newStoreBatch(
        firestore: FirebaseFirestore,
        storeId: String,
        uid: String,
        email: String,
        displayName: String
    ): WriteBatch {
        val store = firestore.collection(FirestoreCollections.STORES).document(storeId)
        return firestore.batch()
            .set(
                store,
                mapOf(
                    FIELD_EMAIL to email,
                    FIELD_DISPLAY_NAME to displayName,
                    FIELD_CREATED_AT to FieldValue.serverTimestamp()
                )
            )
            .set(
                store.collection(FirestoreCollections.MEMBERS).document(uid),
                mapOf(
                    FIELD_ROLE to ROLE_OWNER,
                    FIELD_CREATED_AT to FieldValue.serverTimestamp()
                )
            )
            .set(
                userStores(firestore, uid).document(storeId),
                userStoreFields(displayName)
            )
    }

    fun userStoreFields(displayName: String) = mapOf(
        FIELD_DISPLAY_NAME to displayName,
        FIELD_ROLE to ROLE_OWNER,
        FIELD_CREATED_AT to FieldValue.serverTimestamp()
    )
}