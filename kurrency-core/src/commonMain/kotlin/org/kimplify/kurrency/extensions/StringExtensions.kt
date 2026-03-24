package org.kimplify.kurrency.extensions

import org.kimplify.kurrency.KurrencyLocale

internal fun String.normalizeAmount(locale: KurrencyLocale): String {
    val groupingSep = locale.groupingSeparator
    val decimalSep = locale.decimalSeparator
    var result = this.replace(groupingSep.toString(), "")
        .replace("\u00A0", "")
        .replace("\u202F", "")
    if (decimalSep != '.') {
        result = result.replace(decimalSep, '.')
    }
    return result.trim()
}

internal fun String.normalizeAmount(): String {
    val trimmed = this.trim()
    if (trimmed.isEmpty()) return trimmed

    val sign = if (trimmed.startsWith('-') || trimmed.startsWith('+')) trimmed.first().toString() else ""
    val body = if (sign.isNotEmpty()) trimmed.drop(1) else trimmed

    val strippedOfGroupingWhitespaceAndApostrophes = body
        .replace(" ", "")
        .replace("\u00A0", "")
        .replace("\u202F", "")
        .replace("\u2009", "")
        .replace("'", "")
        .replace("\u2019", "")

    if (strippedOfGroupingWhitespaceAndApostrophes.isEmpty()) return trimmed

    val dotCount = strippedOfGroupingWhitespaceAndApostrophes.count { it == '.' }
    val commaCount = strippedOfGroupingWhitespaceAndApostrophes.count { it == ',' }

    val normalized = when {
        dotCount == 0 && commaCount == 0 -> strippedOfGroupingWhitespaceAndApostrophes

        commaCount == 0 && dotCount == 1 -> strippedOfGroupingWhitespaceAndApostrophes
        commaCount == 0 -> strippedOfGroupingWhitespaceAndApostrophes.replace(".", "")

        dotCount == 0 && commaCount == 1 -> normalizeSingleComma(
            strippedOfGroupingWhitespaceAndApostrophes
        )
        dotCount == 0 -> strippedOfGroupingWhitespaceAndApostrophes.replace(",", "")

        else -> normalizeWithBothSeparators(strippedOfGroupingWhitespaceAndApostrophes)
    }

    return sign + normalized
}

private fun normalizeSingleComma(stripped: String): String {
    val afterComma = stripped.substringAfter(',')
    val isGroupingSeparator = afterComma.length == 3
    return if (isGroupingSeparator) stripped.replace(",", "")
    else stripped.replace(',', '.')
}

private fun normalizeWithBothSeparators(stripped: String): String {
    val lastDot = stripped.lastIndexOf('.')
    val lastComma = stripped.lastIndexOf(',')
    val commaIsDecimal = lastComma > lastDot
    return if (commaIsDecimal) stripped.replace(".", "").replace(',', '.')
    else stripped.replace(",", "")
}
