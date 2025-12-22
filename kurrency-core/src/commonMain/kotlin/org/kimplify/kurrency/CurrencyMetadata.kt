package org.kimplify.kurrency


enum class CurrencyMetadata(
    val code: String,
    val displayName: String,
    val symbol: String,
    val countryIso: String,
    val flag: String,
    val fractionDigits: Int
) {
    USD("USD", "US Dollar", "$", "US", "🇺🇸", 2),
    EUR("EUR", "Euro", "€", "EU", "🇪🇺", 2),
    GBP("GBP", "British Pound", "£", "GB", "🇬🇧", 2),
    JPY("JPY", "Japanese Yen", "¥", "JP", "🇯🇵", 0),
    CNY("CNY", "Chinese Yuan", "¥", "CN", "🇨🇳", 2),
    AUD("AUD", "Australian Dollar", "$", "AU", "🇦🇺", 2),
    CAD("CAD", "Canadian Dollar", "$", "CA", "🇨🇦", 2),
    CHF("CHF", "Swiss Franc", "CHF", "CH", "🇨🇭", 2),
    INR("INR", "Indian Rupee", "₹", "IN", "🇮🇳", 2),
    MXN("MXN", "Mexican Peso", "$", "MX", "🇲🇽", 2),
    BRL("BRL", "Brazilian Real", "R$", "BR", "🇧🇷", 2),
    ZAR("ZAR", "South African Rand", "R", "ZA", "🇿🇦", 2),
    SGD("SGD", "Singapore Dollar", "$", "SG", "🇸🇬", 2),
    HKD("HKD", "Hong Kong Dollar", "$", "HK", "🇭🇰", 2),
    NZD("NZD", "New Zealand Dollar", "$", "NZ", "🇳🇿", 2),
    SEK("SEK", "Swedish Krona", "kr", "SE", "🇸🇪", 2),
    NOK("NOK", "Norwegian Krone", "kr", "NO", "🇳🇴", 2),
    DKK("DKK", "Danish Krone", "kr", "DK", "🇩🇰", 2),
    PLN("PLN", "Polish Zloty", "zł", "PL", "🇵🇱", 2),
    TRY("TRY", "Turkish Lira", "₺", "TR", "🇹🇷", 2),
    RUB("RUB", "Russian Ruble", "₽", "RU", "🇷🇺", 2),
    THB("THB", "Thai Baht", "฿", "TH", "🇹🇭", 2),
    IDR("IDR", "Indonesian Rupiah", "Rp", "ID", "🇮🇩", 2),
    MYR("MYR", "Malaysian Ringgit", "RM", "MY", "🇲🇾", 2),
    PHP("PHP", "Philippine Peso", "₱", "PH", "🇵🇭", 2),
    CZK("CZK", "Czech Koruna", "Kč", "CZ", "🇨🇿", 2),
    ILS("ILS", "Israeli Shekel", "₪", "IL", "🇮🇱", 2),
    CLP("CLP", "Chilean Peso", "$", "CL", "🇨🇱", 0),
    AED("AED", "UAE Dirham", "د.إ", "AE", "🇦🇪", 2),
    SAR("SAR", "Saudi Riyal", "﷼", "SA", "🇸🇦", 2),
    KRW("KRW", "South Korean Won", "₩", "KR", "🇰🇷", 0),
    TWD("TWD", "Taiwan Dollar", "NT$", "TW", "🇹🇼", 2),
    VND("VND", "Vietnamese Dong", "₫", "VN", "🇻🇳", 0),
    ARS("ARS", "Argentine Peso", "$", "AR", "🇦🇷", 2),
    COP("COP", "Colombian Peso", "$", "CO", "🇨🇴", 2),
    PEN("PEN", "Peruvian Sol", "S/", "PE", "🇵🇪", 2),
    UAH("UAH", "Ukrainian Hryvnia", "₴", "UA", "🇺🇦", 2),
    RON("RON", "Romanian Leu", "lei", "RO", "🇷🇴", 2),
    HUF("HUF", "Hungarian Forint", "Ft", "HU", "🇭🇺", 2),
    BGN("BGN", "Bulgarian Lev", "лв", "BG", "🇧🇬", 2),
    PKR("PKR", "Pakistani Rupee", "₨", "PK", "🇵🇰", 2),
    BDT("BDT", "Bangladeshi Taka", "৳", "BD", "🇧🇩", 2),
    LKR("LKR", "Sri Lankan Rupee", "Rs", "LK", "🇱🇰", 2),
    EGP("EGP", "Egyptian Pound", "£", "EG", "🇪🇬", 2),
    NGN("NGN", "Nigerian Naira", "₦", "NG", "🇳🇬", 2),
    KES("KES", "Kenyan Shilling", "KSh", "KE", "🇰🇪", 2),
    TZS("TZS", "Tanzanian Shilling", "TSh", "TZ", "🇹🇿", 2),
    QAR("QAR", "Qatari Riyal", "﷼", "QA", "🇶🇦", 2),
    KWD("KWD", "Kuwaiti Dinar", "د.ك", "KW", "🇰🇼", 3),
    OMR("OMR", "Omani Rial", "﷼", "OM", "🇴🇲", 3);

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
