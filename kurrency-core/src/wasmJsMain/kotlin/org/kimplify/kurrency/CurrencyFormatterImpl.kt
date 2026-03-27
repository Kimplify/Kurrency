@file:OptIn(ExperimentalWasmJsInterop::class)

package org.kimplify.kurrency

@JsFun("function(cur, loc) { return new Intl.NumberFormat(loc || undefined, {style:'currency', currency:cur}).resolvedOptions().maximumFractionDigits; }")
private external fun wasmGetMaxFractionDigits(cur: String, loc: String?): Int

@JsFun("function(cur, loc) { return new Intl.NumberFormat(loc || undefined, {style:'currency', currency:cur}).resolvedOptions().currency; }")
private external fun wasmGetResolvedCurrency(cur: String, loc: String?): String

@JsFun("function(amt, cur, loc) { return new Intl.NumberFormat(loc || undefined, {style:'currency', currency:cur}).format(+amt); }")
private external fun wasmFormatSymbol(amt: String, cur: String, loc: String?): String

@JsFun("function(amt, cur, loc) { return new Intl.NumberFormat(loc || undefined, {style:'currency', currency:cur, currencyDisplay:'code'}).format(+amt); }")
private external fun wasmFormatIso(amt: String, cur: String, loc: String?): String

@JsFun("function(cur) { try { if (typeof Intl.supportedValuesOf === 'function') { return Intl.supportedValuesOf('currency').includes(cur); } return null; } catch(e) { return null; } }")
private external fun wasmIsSupportedCurrency(cur: String): Boolean?

@JsFun("function(cur) { try { new Intl.NumberFormat(undefined, {style:'currency', currency:cur}); return true; } catch(e) { return false; } }")
private external fun wasmCanCreateCurrencyFormatter(cur: String): Boolean

internal actual fun webGetMaxFractionDigits(cur: String, loc: String?): Int = wasmGetMaxFractionDigits(cur, loc)
internal actual fun webGetResolvedCurrency(cur: String, loc: String?): String = wasmGetResolvedCurrency(cur, loc)
internal actual fun webFormatSymbol(amt: String, cur: String, loc: String?): String = wasmFormatSymbol(amt, cur, loc)
internal actual fun webFormatIso(amt: String, cur: String, loc: String?): String = wasmFormatIso(amt, cur, loc)
internal actual fun webIsSupportedCurrency(cur: String): Boolean? = wasmIsSupportedCurrency(cur)
internal actual fun webCanCreateCurrencyFormatter(cur: String): Boolean = wasmCanCreateCurrencyFormatter(cur)

@JsFun("function(amt, cur, loc) { return new Intl.NumberFormat(loc || undefined, {style:'currency', currency:cur, notation:'compact'}).format(+amt); }")
private external fun wasmFormatCompact(amt: String, cur: String, loc: String?): String

internal actual fun webFormatCompact(amt: String, cur: String, loc: String?): String = wasmFormatCompact(amt, cur, loc)
