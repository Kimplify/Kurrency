package org.kimplify.kurrency


enum class CurrencyMetadata(
    val code: String,
    val displayName: String,
    val displayNamePlural: String,
    val symbol: String,
    val countryIso: String,
    val flag: String,
    val fractionDigits: Int,
) {
    USD("USD", "US Dollar", "US Dollars", "$", "US", "🇺🇸", 2),
    EUR("EUR", "Euro", "Euros", "€", "EU", "🇪🇺", 2),
    GBP("GBP", "British Pound", "British Pounds", "£", "GB", "🇬🇧", 2),
    JPY("JPY", "Japanese Yen", "Japanese Yen", "¥", "JP", "🇯🇵", 0),
    CNY("CNY", "Chinese Yuan", "Chinese Yuan", "¥", "CN", "🇨🇳", 2),
    AUD("AUD", "Australian Dollar", "Australian Dollars", "$", "AU", "🇦🇺", 2),
    CAD("CAD", "Canadian Dollar", "Canadian Dollars", "$", "CA", "🇨🇦", 2),
    CHF("CHF", "Swiss Franc", "Swiss Francs", "CHF", "CH", "🇨🇭", 2),
    INR("INR", "Indian Rupee", "Indian Rupees", "₹", "IN", "🇮🇳", 2),
    MXN("MXN", "Mexican Peso", "Mexican Pesos", "$", "MX", "🇲🇽", 2),
    BRL("BRL", "Brazilian Real", "Brazilian Reais", "R$", "BR", "🇧🇷", 2),
    ZAR("ZAR", "South African Rand", "South African Rand", "R", "ZA", "🇿🇦", 2),
    SGD("SGD", "Singapore Dollar", "Singapore Dollars", "$", "SG", "🇸🇬", 2),
    HKD("HKD", "Hong Kong Dollar", "Hong Kong Dollars", "$", "HK", "🇭🇰", 2),
    NZD("NZD", "New Zealand Dollar", "New Zealand Dollars", "$", "NZ", "🇳🇿", 2),
    SEK("SEK", "Swedish Krona", "Swedish Kronor", "kr", "SE", "🇸🇪", 2),
    NOK("NOK", "Norwegian Krone", "Norwegian Kroner", "kr", "NO", "🇳🇴", 2),
    DKK("DKK", "Danish Krone", "Danish Kroner", "kr", "DK", "🇩🇰", 2),
    PLN("PLN", "Polish Zloty", "Polish Zlotys", "zł", "PL", "🇵🇱", 2),
    TRY("TRY", "Turkish Lira", "Turkish Liras", "₺", "TR", "🇹🇷", 2),
    RUB("RUB", "Russian Ruble", "Russian Rubles", "₽", "RU", "🇷🇺", 2),
    THB("THB", "Thai Baht", "Thai Baht", "฿", "TH", "🇹🇭", 2),
    IDR("IDR", "Indonesian Rupiah", "Indonesian Rupiahs", "Rp", "ID", "🇮🇩", 2),
    MYR("MYR", "Malaysian Ringgit", "Malaysian Ringgits", "RM", "MY", "🇲🇾", 2),
    PHP("PHP", "Philippine Peso", "Philippine Pesos", "₱", "PH", "🇵🇭", 2),
    CZK("CZK", "Czech Koruna", "Czech Korunas", "Kč", "CZ", "🇨🇿", 2),
    ILS("ILS", "Israeli Shekel", "Israeli Shekels", "₪", "IL", "🇮🇱", 2),
    CLP("CLP", "Chilean Peso", "Chilean Pesos", "$", "CL", "🇨🇱", 0),
    AED("AED", "UAE Dirham", "UAE Dirhams", "د.إ", "AE", "🇦🇪", 2),
    SAR("SAR", "Saudi Riyal", "Saudi Riyals", "﷼", "SA", "🇸🇦", 2),
    KRW("KRW", "South Korean Won", "South Korean Won", "₩", "KR", "🇰🇷", 0),
    TWD("TWD", "Taiwan Dollar", "Taiwan Dollars", "NT$", "TW", "🇹🇼", 2),
    VND("VND", "Vietnamese Dong", "Vietnamese Dong", "₫", "VN", "🇻🇳", 0),
    ARS("ARS", "Argentine Peso", "Argentine Pesos", "$", "AR", "🇦🇷", 2),
    COP("COP", "Colombian Peso", "Colombian Pesos", "$", "CO", "🇨🇴", 2),
    PEN("PEN", "Peruvian Sol", "Peruvian Soles", "S/", "PE", "🇵🇪", 2),
    UAH("UAH", "Ukrainian Hryvnia", "Ukrainian Hryvnias", "₴", "UA", "🇺🇦", 2),
    RON("RON", "Romanian Leu", "Romanian Lei", "lei", "RO", "🇷🇴", 2),
    HUF("HUF", "Hungarian Forint", "Hungarian Forints", "Ft", "HU", "🇭🇺", 2),
    BGN("BGN", "Bulgarian Lev", "Bulgarian Leva", "лв", "BG", "🇧🇬", 2),
    PKR("PKR", "Pakistani Rupee", "Pakistani Rupees", "₨", "PK", "🇵🇰", 2),
    BDT("BDT", "Bangladeshi Taka", "Bangladeshi Taka", "৳", "BD", "🇧🇩", 2),
    LKR("LKR", "Sri Lankan Rupee", "Sri Lankan Rupees", "Rs", "LK", "🇱🇰", 2),
    EGP("EGP", "Egyptian Pound", "Egyptian Pounds", "£", "EG", "🇪🇬", 2),
    NGN("NGN", "Nigerian Naira", "Nigerian Naira", "₦", "NG", "🇳🇬", 2),
    KES("KES", "Kenyan Shilling", "Kenyan Shillings", "KSh", "KE", "🇰🇪", 2),
    TZS("TZS", "Tanzanian Shilling", "Tanzanian Shillings", "TSh", "TZ", "🇹🇿", 2),
    QAR("QAR", "Qatari Riyal", "Qatari Riyals", "﷼", "QA", "🇶🇦", 2),
    KWD("KWD", "Kuwaiti Dinar", "Kuwaiti Dinars", "د.ك", "KW", "🇰🇼", 3),
    OMR("OMR", "Omani Rial", "Omani Rials", "﷼", "OM", "🇴🇲", 3);

    companion object {
        private val codeMap by lazy {
            KurrencyLog.d { "Initializing CurrencyMetadata map with ${entries.size} currencies" }
            entries.associateBy { it.code.uppercase() }
        }

        fun parse(code: String): Result<CurrencyMetadata> {
            if (code.isBlank()) {
                val error = KurrencyError.InvalidCurrencyCode(code)
                KurrencyLog.w { error.errorMessage }
                return Result.failure(error)
            }

            val normalizedCode = code.uppercase().trim()
            KurrencyLog.d { "Parsing currency code: $normalizedCode" }

            return codeMap[normalizedCode]?.let { metadata ->
                KurrencyLog.d { "Successfully parsed currency: ${metadata.displayName} ${metadata.flag}" }
                Result.success(metadata)
            } ?: run {
                val error = KurrencyError.InvalidCurrencyCode(code)
                KurrencyLog.w { error.errorMessage }
                Result.failure(error)
            }
        }

        fun getAll(): List<CurrencyMetadata> {
            KurrencyLog.d { "Retrieving all ${entries.size} currencies" }
            return entries
        }
    }
}
