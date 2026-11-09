@file:Suppress("MatchingDeclarationName")

package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

/**
 * Padrões de teclado para o Lucro Varejo.
 *
 * Garante que:
 * - Inputs comuns iniciem com a primeira letra maiúscula ([KeyboardCapitalization.Sentences]).
 * - Campos de e-mail, senha e numéricos não usem capitalização automática ([KeyboardCapitalization.None]).
 */
object AppKeyboardDefaults {
    val common: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        keyboardType = KeyboardType.Text
    )

    val email: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Email
    )

    val password: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Password
    )

    val number: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Number
    )

    val decimal: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Decimal
    )
}

/**
 * Retorna a capitalização padrão para o [KeyboardType] informado.
 * E-mails, senhas e campos numéricos usam [KeyboardCapitalization.None].
 * Demais entradas de texto usam [KeyboardCapitalization.Sentences] (primeira letra maiúscula).
 */
fun defaultCapitalizationFor(keyboardType: KeyboardType): KeyboardCapitalization =
    when (keyboardType) {
        KeyboardType.Email,
        KeyboardType.Password,
        KeyboardType.Number,
        KeyboardType.Decimal,
        KeyboardType.Phone,
        KeyboardType.NumberPassword -> KeyboardCapitalization.None
        else -> KeyboardCapitalization.Sentences
    }

/**
 * Cria [KeyboardOptions] aplicando a capitalização padrão do app quando não for explicitamente informada:
 * - Se for [KeyboardType.Email], [KeyboardType.Password], [KeyboardType.Number] ou [KeyboardType.Decimal]: [KeyboardCapitalization.None].
 * - Caso contrário: [KeyboardCapitalization.Sentences] (primeira letra maiúscula).
 */
fun appKeyboardOptions(
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = defaultCapitalizationFor(keyboardType),
    imeAction: ImeAction = ImeAction.Default,
): KeyboardOptions = KeyboardOptions(
    capitalization = capitalization,
    keyboardType = keyboardType,
    imeAction = imeAction
)
