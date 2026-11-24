package com.cabral.lucrovarejo.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ShoppingBag
import org.junit.Assert.assertSame
import org.junit.Test

class ProductIconTest {
    @Test
    fun bolsaUsesShoppingBag() {
        assertSame(Icons.Outlined.ShoppingBag, productIconFor("Bolsa de couro"))
    }

    @Test
    fun carteiraUsesWallet() {
        assertSame(Icons.Outlined.AccountBalanceWallet, productIconFor("Carteira masculina"))
    }

    @Test
    fun meiaAndMeiasUseSock() {
        assertSame(SockIcon, productIconFor("Meia esportiva"))
        assertSame(SockIcon, productIconFor("Kit de meias"))
    }

    @Test
    fun shoeKeywordsUseSneaker() {
        listOf("Sapato social", "Calçado infantil", "Calcado", "Tênis Nike", "tenis corrida")
            .forEach { assertSame(it, SneakerIcon, productIconFor(it)) }
    }

    @Test
    fun chineloUsesSlipper() {
        assertSame(SlipperIcon, productIconFor("Chinelo Havaianas"))
    }

    @Test
    fun umbrellaKeywordsUseUmbrella() {
        listOf("Guarda chuva", "Guarda-chuva", "Guardachuva", "GUARDA-CHUVA grande", "Sombrinha", "Pontaria")
            .forEach { assertSame(it, UmbrellaIcon, productIconFor(it)) }
    }

    @Test
    fun matchingIgnoresCase() {
        assertSame(Icons.Outlined.ShoppingBag, productIconFor("BOLSA"))
        assertSame(SockIcon, productIconFor("MEIAS"))
    }

    @Test
    fun unknownTextUsesGenericIcon() {
        assertSame(Icons.Outlined.Inventory2, productIconFor("Camiseta"))
        assertSame(Icons.Outlined.Inventory2, productIconFor(""))
    }
}


