package com.cabral.lucrovarejo.data.purchase.datasource

import com.cabral.lucrovarejo.data.purchase.model.PurchaseRecord
import com.google.firebase.firestore.DocumentSnapshot

internal fun DocumentSnapshot.toPurchaseRecord(): PurchaseRecord = PurchaseRecord(
    documentId = id,
    cod = requireNotNull(getString("cod")) { "Purchase $id has no valid 'cod' field" },
    name = requireNotNull(getString("name")) { "Purchase $id has no valid 'name' field" },
    cost = Math.toIntExact(requireNotNull(getLong("cost")) {
        "Purchase $id has no valid 'cost' field"
    }),
    description = requireNotNull(getString("description")) {
        "Purchase $id has no valid 'description' field"
    },
    salePrice = Math.toIntExact(requireNotNull(getLong("sale_price")) {
        "Purchase $id has no valid 'sale_price' field"
    }),
    quantity = Math.toIntExact(requireNotNull(getLong("quantity")) {
        "Purchase $id has no valid 'quantity' field"
    }),
    saleDate = requireNotNull(getTimestamp("sale_date")) {
        "Purchase $id has no valid 'sale_date' field"
    }.toDate(),
    paymentMethod = requireNotNull(getString("payment_method")) {
        "Purchase $id has no valid 'payment_method' field"
    },
    terminalId = getString("terminal_id"),
    terminalName = getString("terminal_name"),
    brand = getString("brand"),
    installments = getLong("installments")?.let(Math::toIntExact),
    // Vendas antigas não têm snapshot de tarifa: assumem sem taxa.
    tariffRateBps = getLong("tariff_rate_bps")?.let(Math::toIntExact) ?: 0,
    tariffId = getString("tariff_id"),
    tariffAmountCents = getLong("tariff_amount_cents") ?: 0L
)
