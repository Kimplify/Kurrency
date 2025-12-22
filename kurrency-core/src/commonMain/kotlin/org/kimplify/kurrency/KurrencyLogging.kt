package org.kimplify.kurrency

import org.kimplify.cedar.logging.Cedar

interface KurrencyLogger {
    fun debug(tag: String, message: String)
    fun warn(tag: String, message: String)
    fun error(tag: String, message: String, throwable: Throwable)
}

enum class KurrencyLogLevel {
    Debug,
    Warn,
    Error,
    None
}

object KurrencyLogging {
    var enabled: Boolean = false
    var minLevel: KurrencyLogLevel = KurrencyLogLevel.Debug
    var logger: KurrencyLogger = CedarLogger()
}

class CedarLogger : KurrencyLogger {
    override fun debug(tag: String, message: String) {
        Cedar.tag(tag).d(message)
    }

    override fun warn(tag: String, message: String) {
        Cedar.tag(tag).w(message)
    }

    override fun error(tag: String, message: String, throwable: Throwable) {
        Cedar.tag(tag).e(throwable, message)
    }
}

internal object KurrencyLog {
    private const val TAG = "Kurrency"

    inline fun d(message: () -> String) {
        log(KurrencyLogLevel.Debug) { it.debug(TAG, message()) }
    }

    inline fun w(message: () -> String) {
        log(KurrencyLogLevel.Warn) { it.warn(TAG, message()) }
    }

    inline fun e(throwable: Throwable, message: () -> String) {
        log(KurrencyLogLevel.Error) { it.error(TAG, message(), throwable) }
    }

    inline fun log(level: KurrencyLogLevel, block: (KurrencyLogger) -> Unit) {
        if (!isEnabled(level)) return
        block(KurrencyLogging.logger)
    }

    private fun isEnabled(level: KurrencyLogLevel): Boolean {
        if (!KurrencyLogging.enabled) return false
        val minLevel = KurrencyLogging.minLevel
        if (minLevel == KurrencyLogLevel.None) return false
        return level.ordinal >= minLevel.ordinal
    }
}
