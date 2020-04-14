package io.sentry.android.core

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.sentry.core.IHub
import kotlin.test.Ignore
import kotlin.test.Test

class LifecycleWatcherTest {

    @Test
    fun `if last started session is 0, start new session`() {
        val hub = mock<IHub>()
        val watcher = LifecycleWatcher(hub, 100L, true, false)
        watcher.onStart(mock())
        verify(hub).startSession()
    }

    @Test
    fun `if last started session is after interval, start new session`() {
        val hub = mock<IHub>()
        val watcher = LifecycleWatcher(hub, 100L, true, false)
        watcher.onStart(mock())
        Thread.sleep(150L)
        watcher.onStart(mock())
        verify(hub, times(2)).startSession()
    }

    @Test
    fun `if last started session is before interval, it should not start a new session`() {
        val hub = mock<IHub>()
        val watcher = LifecycleWatcher(hub, 1000L, true, false)
        watcher.onStart(mock())
        Thread.sleep(100)
        watcher.onStart(mock())
        verify(hub).startSession()
    }

    @Ignore("for some reason this is flaky only on appveyor")
    @Test
    fun `if app goes to background, end session after interval`() {
        val hub = mock<IHub>()
        val watcher = LifecycleWatcher(hub, 100L, true, false)
        watcher.onStart(mock())
        watcher.onStop(mock())
        Thread.sleep(500L)
        verify(hub).endSession()
    }

    @Test
    fun `if app goes to background and foreground again, dont end the session`() {
        val hub = mock<IHub>()
        val watcher = LifecycleWatcher(hub, 1000L, true, false)
        watcher.onStart(mock())
        watcher.onStop(mock())
        Thread.sleep(150)
        watcher.onStart(mock())
        verify(hub, never()).endSession()
    }

    @Test
    fun `When session tracking is disabled, do not start session`() {
        val hub = mock<IHub>()
        val watcher = LifecycleWatcher(hub, 1000L, false, false)
        watcher.onStart(mock())
        verify(hub, never()).startSession()
    }

    @Test
    fun `When session tracking is disabled, do not end session`() {
        val hub = mock<IHub>()
        val watcher = LifecycleWatcher(hub, 0L, false, false)
        watcher.onStart(mock())
        verify(hub, never()).endSession()
    }
}
