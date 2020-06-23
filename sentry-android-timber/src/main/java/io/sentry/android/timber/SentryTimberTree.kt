package io.sentry.android.timber

import android.util.Log
import io.sentry.core.Breadcrumb
import io.sentry.core.IHub
import io.sentry.core.SentryEvent
import io.sentry.core.SentryLevel
import io.sentry.core.protocol.Message
import timber.log.Timber

/**
 * Sentry Timber tree which is responsible to capture events via Timber
 */
class SentryTimberTree(
    private val hub: IHub,
    private val minEventLevel: SentryLevel,
    private val minBreadcrumbLevel: SentryLevel
) : Timber.Tree() {

    /**
     * do not log if it's lower than min. required level.
     */
    private fun isLoggable(level: SentryLevel, minLevel: SentryLevel): Boolean = level.ordinal >= minLevel.ordinal

    /**
     * Compares the minLevel given by the user and the level given by the event
     * to capture an event or not.
     */
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        val level = getSentryLevel(priority)

        // checks only the event level
        return isLoggable(level, minEventLevel)
    }

    /**
     * Captures a Sentry Event if the min. level is equal or higher than the min. required level.
     */
    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        val level = getSentryLevel(priority)

        captureEvent(level, tag, message, throwable)
        addBreadcrumbIfEventHasThrowable(level, throwable, message)
    }

    /**
     * Captures an event with the given attributes
     */
    private fun captureEvent(level: SentryLevel, tag: String?, message: String, throwable: Throwable?) {
        val sentryEvent = SentryEvent()
        sentryEvent.level = level

        // if there's no throwable, should it be a breadcrumb then?
        throwable?.let {
            sentryEvent.setThrowable(it)
        }
        sentryEvent.message = Message().apply {
            formatted = message
        }

        tag?.let {
            sentryEvent.setTag("TimberTag", it)
        }
        // maybe this should add itself to the integration package
        // we need to define a merging mechanism then
        sentryEvent.setTag("origin", "SentryTimberIntegration")

        hub.captureEvent(sentryEvent)
    }

    /**
     * Adds a breadcrumb if the event has an exception.
     */
    private fun addBreadcrumbIfEventHasThrowable(level: SentryLevel, throwable: Throwable?, message: String) {
        // checks the breadcrumb level
        if (throwable != null && isLoggable(level, minBreadcrumbLevel)) {
            val breadCrumb = Breadcrumb()
            breadCrumb.level = level
            breadCrumb.category = "exception"
            breadCrumb.type = "error"
            breadCrumb.message = message

            hub.addBreadcrumb(breadCrumb)
        }
    }

    /**
     * Converts from Timber priority to SentryLevel.
     * Fallback to SentryLevel.DEBUG.
     */
    private fun getSentryLevel(priority: Int): SentryLevel {
        return when (priority) {
            Log.ASSERT -> SentryLevel.FATAL
            Log.ERROR -> SentryLevel.ERROR
            Log.WARN -> SentryLevel.WARNING
            Log.INFO -> SentryLevel.INFO
            Log.DEBUG -> SentryLevel.DEBUG
            Log.VERBOSE -> SentryLevel.DEBUG
            else -> SentryLevel.DEBUG
        }
    }
}
