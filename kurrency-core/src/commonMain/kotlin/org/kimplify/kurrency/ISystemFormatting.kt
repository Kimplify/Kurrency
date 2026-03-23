package org.kimplify.kurrency

internal interface SystemFormattingProvider {
    val decimalSeparator: String
    val groupingSeparator: String
}