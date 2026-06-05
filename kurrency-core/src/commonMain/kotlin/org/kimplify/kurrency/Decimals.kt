package org.kimplify.kurrency

internal object Decimals {

    fun isZero(s: String): Boolean = s.none { it in '1'..'9' }

    fun isNegative(s: String): Boolean = s.startsWith("-") && !isZero(s)

    fun abs(s: String): String = if (s.startsWith("-") || s.startsWith("+")) s.substring(1) else s

    fun isOne(s: String): Boolean {
        val a = abs(s)
        val dot = a.indexOf('.')
        val intPart = (if (dot >= 0) a.substring(0, dot) else a).trimStart('0').ifEmpty { "0" }
        val fracPart = if (dot >= 0) a.substring(dot + 1) else ""
        return intPart == "1" && fracPart.all { it == '0' }
    }

    fun roundToScale(s: String, scale: Int, mode: RoundingMode): String {
        val dot = s.indexOf('.')
        val intDigits = (if (dot >= 0) s.substring(0, dot) else s).ifEmpty { "0" }
        val fracPart = if (dot >= 0) s.substring(dot + 1) else ""

        if (fracPart.length <= scale) {
            val padded = fracPart.padEnd(scale, '0')
            return if (scale == 0) intDigits else "$intDigits.$padded"
        }

        val kept = fracPart.substring(0, scale)
        val dropped = fracPart.substring(scale)
        val combined = intDigits + kept

        val roundUp = when (mode) {
            RoundingMode.DOWN -> false
            RoundingMode.UP -> dropped.any { it != '0' }
            RoundingMode.HALF_UP -> dropped[0] >= '5'
            RoundingMode.HALF_EVEN -> when {
                dropped[0] > '5' -> true
                dropped[0] < '5' -> false
                dropped.drop(1).any { it != '0' } -> true
                else -> (combined.last() - '0') % 2 == 1
            }
        }

        val resultDigits = if (roundUp) incrementDigits(combined) else combined
        val intResult = (if (scale == 0) resultDigits else resultDigits.dropLast(scale))
            .trimStart('0').ifEmpty { "0" }
        val fracResult = if (scale == 0) "" else resultDigits.takeLast(scale)
        return if (scale == 0) intResult else "$intResult.$fracResult"
    }

    fun expandScientific(s: String): String {
        if (s.none { it == 'e' || it == 'E' }) return s
        val sign = if (s.startsWith("-")) "-" else ""
        val unsigned = if (s.startsWith("-") || s.startsWith("+")) s.substring(1) else s
        val ei = unsigned.indexOfFirst { it == 'e' || it == 'E' }
        val mantissa = unsigned.substring(0, ei)
        val exp = unsigned.substring(ei + 1).toIntOrNull() ?: return s
        val dot = mantissa.indexOf('.')
        val intDigits = if (dot >= 0) mantissa.substring(0, dot) else mantissa
        val fracDigits = if (dot >= 0) mantissa.substring(dot + 1) else ""
        val digits = intDigits + fracDigits
        val pointPos = intDigits.length + exp
        val body = when {
            pointPos <= 0 -> "0." + "0".repeat(-pointPos) + digits
            pointPos >= digits.length -> digits + "0".repeat(pointPos - digits.length)
            else -> digits.substring(0, pointPos) + "." + digits.substring(pointPos)
        }
        return sign + body
    }

    private fun incrementDigits(digits: String): String {
        val chars = digits.toCharArray()
        var i = chars.size - 1
        while (i >= 0) {
            if (chars[i] == '9') {
                chars[i] = '0'
                i--
            } else {
                chars[i] = chars[i] + 1
                return chars.concatToString()
            }
        }
        return "1" + chars.concatToString()
    }
}
