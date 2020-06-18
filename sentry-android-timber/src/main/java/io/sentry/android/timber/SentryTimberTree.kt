package io.sentry.android.timber

import android.util.Log
import io.sentry.core.IHub
import io.sentry.core.SentryEvent
import io.sentry.core.SentryLevel
import io.sentry.core.protocol.Message
import timber.log.Timber

/**
 * Sentry Timber tree which is responsible to capture events via Timber
 */
class SentryTimberTree(private val hub: IHub, private val minLevel: SentryLevel) : Timber.Tree() {

    /**
     * Compares the minLevel given by the user and the level given by the event
     * to capture an event or not.
     */
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        val sentryLevel = getSentryLevel(priority)

        // do not log if it's lower than min. required level.
        return sentryLevel.ordinal >= minLevel.ordinal
    }

    /**
     * Captures a Sentry Event if the min. level is equal or higher than the min. required level.
     */
    override fun log(priority: Int, tag: String?, message: String, throwable: Throwable?) {
        val sentryEvent = SentryEvent()
        sentryEvent.level = getSentryLevel(priority)

        throwable?.let {
            sentryEvent.setThrowable(it)
        }

        // timber concatenates the stacktrace to the exception if the exception is not null
        // let's take only the 1st line as this is probably the only message written by the user.
        // or the 1st line of the exception which is probably the same as event.title
        // eg java.lang.Exception: java.lang.Exception: java.lang.Exception: Some exception.
        // timber should offer a way of disabling it
        val realMessage = message.splitToSequence(delimiters = *arrayOf("\n"))
        val sentryMessage = Message()

        sentryMessage.formatted = realMessage.first()
        sentryEvent.message = sentryMessage

        tag?.let {
            sentryEvent.setTag("TimberTag", it)
        }
        // maybe this should add itself to the integration package
        // we need to define a merging mechanism then
        sentryEvent.setTag("origin", "SentryTimberIntegration")

        hub.captureEvent(sentryEvent)
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
