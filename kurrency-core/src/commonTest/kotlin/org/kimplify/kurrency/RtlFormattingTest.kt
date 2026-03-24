package org.kimplify.kurrency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RtlFormattingTest {

    // ---- isRightToLeft: LTR locales ----

    @Test
    fun testUS_isLTR() {
        assertFalse(KurrencyLocale.US.isRightToLeft)
    }

    @Test
    fun testUK_isLTR() {
        assertFalse(KurrencyLocale.UK.isRightToLeft)
    }

    @Test
    fun testGermany_isLTR() {
        assertFalse(KurrencyLocale.GERMANY.isRightToLeft)
    }

    @Test
    fun testFrance_isLTR() {
        assertFalse(KurrencyLocale.FRANCE.isRightToLeft)
    }

    @Test
    fun testJapan_isLTR() {
        assertFalse(KurrencyLocale.JAPAN.isRightToLeft)
    }

    @Test
    fun testChina_isLTR() {
        assertFalse(KurrencyLocale.CHINA.isRightToLeft)
    }

    @Test
    fun testIndia_isLTR() {
        assertFalse(KurrencyLocale.INDIA.isRightToLeft)
    }

    @Test
    fun testBrazil_isLTR() {
        assertFalse(KurrencyLocale.BRAZIL.isRightToLeft)
    }

    @Test
    fun testKorea_isLTR() {
        assertFalse(KurrencyLocale.KOREA.isRightToLeft)
    }

    // ---- isRightToLeft: RTL locales ----

    @Test
    fun testSaudiArabia_isRTL() {
        assertTrue(KurrencyLocale.SAUDI_ARABIA.isRightToLeft)
    }

    @Test
    fun testArabicEG_isRTL() {
        assertTrue(KurrencyLocale.ARABIC_EG.isRightToLeft)
    }

    @Test
    fun testHebrew_isRTL() {
        assertTrue(KurrencyLocale.HEBREW.isRightToLeft)
    }

    @Test
    fun testPersian_isRTL() {
        assertTrue(KurrencyLocale.PERSIAN.isRightToLeft)
    }

    @Test
    fun testUrdu_isRTL() {
        assertTrue(KurrencyLocale.URDU.isRightToLeft)
    }

    // ---- numeralSystem ----

    @Test
    fun testUS_numeralSystem_isWestern() {
        assertEquals(NumeralSystem.WESTERN, KurrencyLocale.US.numeralSystem)
    }

    @Test
    fun testGermany_numeralSystem_isWestern() {
        assertEquals(NumeralSystem.WESTERN, KurrencyLocale.GERMANY.numeralSystem)
    }

    @Test
    fun testJapan_numeralSystem_isWestern() {
        assertEquals(NumeralSystem.WESTERN, KurrencyLocale.JAPAN.numeralSystem)
    }

    @Test
    fun testHebrew_numeralSystem_isWestern() {
        assertEquals(NumeralSystem.WESTERN, KurrencyLocale.HEBREW.numeralSystem)
    }

    @Test
    fun testSaudiArabia_numeralSystem_isEasternArabic() {
        assertEquals(NumeralSystem.EASTERN_ARABIC, KurrencyLocale.SAUDI_ARABIA.numeralSystem)
    }

    @Test
    fun testArabicEG_numeralSystem_isEasternArabic() {
        assertEquals(NumeralSystem.EASTERN_ARABIC, KurrencyLocale.ARABIC_EG.numeralSystem)
    }

    @Test
    fun testPersian_numeralSystem_isPersian() {
        assertEquals(NumeralSystem.PERSIAN, KurrencyLocale.PERSIAN.numeralSystem)
    }

    @Test
    fun testUrdu_numeralSystem_isEasternArabic() {
        assertEquals(NumeralSystem.EASTERN_ARABIC, KurrencyLocale.URDU.numeralSystem)
    }

    // ---- RTL locale formatting produces non-empty output ----

    @Test
    fun testSaudiArabia_formatSAR_nonEmpty() {
        val formatter = CurrencyFormatter(KurrencyLocale.SAUDI_ARABIA)
        val result = formatter.formatCurrencyStyle("100.00", "SAR")
        assertTrue(result.isNotEmpty(), "SAR formatting should produce non-empty output")
    }

    @Test
    fun testHebrew_formatILS_nonEmpty() {
        val formatter = CurrencyFormatter(KurrencyLocale.HEBREW)
        val result = formatter.formatCurrencyStyle("100.00", "ILS")
        assertTrue(result.isNotEmpty(), "ILS formatting should produce non-empty output")
    }

    @Test
    fun testArabicEG_formatEGP_nonEmpty() {
        val formatter = CurrencyFormatter(KurrencyLocale.ARABIC_EG)
        val result = formatter.formatCurrencyStyle("100.00", "EGP")
        assertTrue(result.isNotEmpty(), "EGP formatting should produce non-empty output")
    }

    @Test
    fun testPersian_formatIRR_nonEmpty() {
        val formatter = CurrencyFormatter(KurrencyLocale.PERSIAN)
        val result = formatter.formatCurrencyStyle("100.00", "IRR")
        assertTrue(result.isNotEmpty(), "IRR formatting should produce non-empty output")
    }

    @Test
    fun testUrdu_formatPKR_nonEmpty() {
        val formatter = CurrencyFormatter(KurrencyLocale.URDU)
        val result = formatter.formatCurrencyStyle("100.00", "PKR")
        assertTrue(result.isNotEmpty(), "PKR formatting should produce non-empty output")
    }

    // ---- Locale language tags ----

    @Test
    fun testArabicEG_languageTag() {
        assertTrue(
            KurrencyLocale.ARABIC_EG.languageTag.startsWith("ar"),
            "ARABIC_EG language tag should start with 'ar': ${KurrencyLocale.ARABIC_EG.languageTag}"
        )
    }

    @Test
    fun testHebrew_languageTag() {
        assertTrue(
            KurrencyLocale.HEBREW.languageTag.startsWith("he") ||
                KurrencyLocale.HEBREW.languageTag.startsWith("iw"),
            "HEBREW language tag should start with 'he' or 'iw': ${KurrencyLocale.HEBREW.languageTag}"
        )
    }

    @Test
    fun testPersian_languageTag() {
        assertTrue(
            KurrencyLocale.PERSIAN.languageTag.startsWith("fa"),
            "PERSIAN language tag should start with 'fa': ${KurrencyLocale.PERSIAN.languageTag}"
        )
    }

    @Test
    fun testUrdu_languageTag() {
        assertTrue(
            KurrencyLocale.URDU.languageTag.startsWith("ur"),
            "URDU language tag should start with 'ur': ${KurrencyLocale.URDU.languageTag}"
        )
    }
}
