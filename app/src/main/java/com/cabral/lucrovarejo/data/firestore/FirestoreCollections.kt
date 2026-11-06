package com.cabral.lucrovarejo.data.firestore

import com.cabral.lucrovarejo.BuildConfig

// Nomes de colecoes do Firestore, definidos em um unico lugar.
internal object FirestoreCollections {
    // "stores" na release e "stores_debug" no debug (ver buildConfigField em app/build.gradle.kts).
    const val STORES = BuildConfig.STORES_COLLECTION

    // Atalho opcional para listar as lojas do usuario; nao concede acesso.
    const val USERS = BuildConfig.USERS_COLLECTION
    // Subcolecao de users/{uid}; nome igual em debug e release.
    const val USER_STORES = "stores"

    const val MEMBERS = "members"
    const val PRODUCTS = "products"
    const val PURCHASES = "purchases"
    const val PAYMENT_TERMINALS = "paymentTerminals"
    const val TARIFFS = "tariffs"
}
