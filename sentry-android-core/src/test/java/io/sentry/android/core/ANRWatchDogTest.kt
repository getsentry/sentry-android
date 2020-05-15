package io.sentry.android.core

import android.app.ActivityManager
import android.app.ActivityManager.ProcessErrorStateInfo.NOT_RESPONDING
import android.app.ActivityManager.ProcessErrorStateInfo.NO_ERROR
import android.content.Context
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.sentry.android.core.ANRWatchDog.ANRListener
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ANRWatchDogTest {
    @Test
    fun `when ANR is detected, callback is invoked with threads stacktrace`() {
        var anr: ApplicationNotResponding? = null
        val handler = mock<IHandler>()
        val thread = mock<Thread>()
        val expectedState = Thread.State.BLOCKED
        val stacktrace = StackTraceElement("class", "method", "fileName", 10)
        whenever(thread.state).thenReturn(expectedState)
        whenever(thread.stackTrace).thenReturn(arrayOf(stacktrace))
        val latch = CountDownLatch(1)
        whenever(handler.post(any())).then { latch.countDown() }
        whenever(handler.thread).thenReturn(thread)
        val interval = 1L
        val sut = ANRWatchDog(interval, true, ANRListener { a -> anr = a }, mock(), handler, mock())
        val es = Executors.newSingleThreadExecutor()
        try {
            es.submit { sut.run() }

            assertTrue(latch.await(10L, TimeUnit.SECONDS)) // Wait until worker posts the job for the "UI thread"
            var waitCount = 0
            do {
                Thread.sleep(100) // Let worker realize this is ANR
            } while (anr == null && waitCount++ < 100)

            assertNotNull(anr)
            assertEquals(expectedState, anr!!.thread.state)
            assertEquals(stacktrace.className, anr!!.stackTrace[0].className)
        } finally {
            sut.interrupt()
            es.shutdown()
        }
    }

    @Test
    fun `when ANR is not detected, callback is not invoked`() {
        var anr: ApplicationNotResponding? = null
        val handler = mock<IHandler>()
        val thread = mock<Thread>()
        var invoked = false
        whenever(handler.post(any())).then { i ->
            invoked = true
            (i.getArgument(0) as Runnable).run()
        }
        whenever(handler.thread).thenReturn(thread)
        val interval = 1L
        val sut = ANRWatchDog(interval, true, ANRListener { a -> anr = a }, mock(), handler, mock())
        val es = Executors.newSingleThreadExecutor()
        try {
            es.submit { sut.run() }

            var waitCount = 0
            do {
                Thread.sleep(100) // Let worker realize his runner always runs
            } while (!invoked && waitCount++ < 100)

            assertTrue(invoked)
            assertNull(anr) // callback never ran
        } finally {
            sut.interrupt()
            es.shutdown()
        }
    }

    @Test
    fun `when ANR is detected and ActivityManager has ANR process, callback not invoked`() {
        var anr: ApplicationNotResponding? = null
        val handler = mock<IHandler>()
        val thread = mock<Thread>()
        val expectedState = Thread.State.BLOCKED
        val stacktrace = StackTraceElement("class", "method", "fileName", 10)
        whenever(thread.state).thenReturn(expectedState)
        whenever(thread.stackTrace).thenReturn(arrayOf(stacktrace))
        val latch = CountDownLatch(1)
        whenever(handler.post(any())).then { latch.countDown() }
        whenever(handler.thread).thenReturn(thread)
        val interval = 1L
        val context = mock<Context>()
        val am = mock<ActivityManager>()

        whenever(context.getSystemService(eq(Context.ACTIVITY_SERVICE))).thenReturn(am)
        val stateInfo = ActivityManager.ProcessErrorStateInfo()
        stateInfo.condition = NOT_RESPONDING
        val anrs = listOf(stateInfo)
        whenever(am.processesInErrorState).thenReturn(anrs)
        val sut = ANRWatchDog(interval, true, ANRListener { a -> anr = a }, mock(), handler, context)
        val es = Executors.newSingleThreadExecutor()
        try {
            es.submit { sut.run() }

            assertTrue(latch.await(10L, TimeUnit.SECONDS)) // Wait until worker posts the job for the "UI thread"
            var waitCount = 0
            do {
                Thread.sleep(100) // Let worker realize this is ANR
            } while (anr == null && waitCount++ < 100)

            assertNotNull(anr)
            assertEquals(expectedState, anr!!.thread.state)
            assertEquals(stacktrace.className, anr!!.stackTrace[0].className)
        } finally {
            sut.interrupt()
            es.shutdown()
        }
    }

    @Test
    fun `when ANR is detected and ActivityManager has not ANR process, callback is not invoked`() {
        var anr: ApplicationNotResponding? = null
        val handler = mock<IHandler>()
        val thread = mock<Thread>()
        val expectedState = Thread.State.BLOCKED
        val stacktrace = StackTraceElement("class", "method", "fileName", 10)
        whenever(thread.state).thenReturn(expectedState)
        whenever(thread.stackTrace).thenReturn(arrayOf(stacktrace))
        val latch = CountDownLatch(1)
        whenever(handler.post(any())).then { latch.countDown() }
        whenever(handler.thread).thenReturn(thread)
        val interval = 1L
        val context = mock<Context>()
        val am = mock<ActivityManager>()

        whenever(context.getSystemService(eq(Context.ACTIVITY_SERVICE))).thenReturn(am)
        val stateInfo = ActivityManager.ProcessErrorStateInfo()
        stateInfo.condition = NO_ERROR
        val anrs = listOf(stateInfo)
        whenever(am.processesInErrorState).thenReturn(anrs)
        val sut = ANRWatchDog(interval, true, ANRListener { a -> anr = a }, mock(), handler, context)
        val es = Executors.newSingleThreadExecutor()
        try {
            es.submit { sut.run() }

            assertTrue(latch.await(10L, TimeUnit.SECONDS)) // Wait until worker posts the job for the "UI thread"
            var waitCount = 0
            do {
                Thread.sleep(100) // Let worker realize this is ANR
            } while (anr == null && waitCount++ < 100)
            assertNull(anr) // callback never ran
        } finally {
            sut.interrupt()
            es.shutdown()
        }
    }
}
