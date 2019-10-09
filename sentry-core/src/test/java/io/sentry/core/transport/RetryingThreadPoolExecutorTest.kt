package io.sentry.core.transport

import io.sentry.core.transport.Retryable
import io.sentry.core.transport.RetryingThreadPoolExecutor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertTrue
import kotlin.test.assertEquals
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.AfterTest

class RetryingThreadPoolExecutorTest {
    private val maxRetries = 5
    private var threadPool: RetryingThreadPoolExecutor? = null

    @BeforeTest
    fun setup() {
        val threadFactory = ThreadFactory { r ->
            val t = Thread(r, "RetryingScheduledThreadPoolExecutorTestThread")
            t.isDaemon = true
            t
        }
        val rerunImmediately = io.sentry.core.transport.IBackOffIntervalStrategy { 0L }
        threadPool = RetryingThreadPoolExecutor(1, maxRetries, threadFactory, rerunImmediately, DiscardPolicy())
    }

    @AfterTest
    fun teardown() {
        threadPool?.shutdownNow()
    }

    @Test
    fun `executes once when finishes ok`() {
        val counter = CountDownLatch(1)
        val actualTimes = AtomicInteger()
        threadPool?.submit {
            counter.countDown()
            actualTimes.incrementAndGet()
        }

        counter.await()
        // wait to see if there are any more attempts
        Thread.sleep(1000)

        assertEquals(1, actualTimes.get(), "Successful task should only be run once.")
    }

    @Test
    fun `retries while failing`() {
        val counter = CountDownLatch(3)
        val actualTimes = AtomicInteger()

        threadPool?.submit {
            actualTimes.incrementAndGet()
            counter.countDown()
            if (counter.count > 0) {
                throw RuntimeException()
            }
        }

        val threeTimes = counter.await(1, TimeUnit.MINUTES)
        // wait to see if there are any more attempts
        Thread.sleep(1000)

        assertTrue(threeTimes, "Should have retried 3 times but didn't in 1 minute.")
        assertEquals(3, actualTimes.get(), "Shouldn't see any more attempts after 3 failures, but saw some")
    }

    @Test
    fun `retries at most maxRetries-times`() {
        val counter = CountDownLatch(maxRetries)
        val actualTimes = AtomicInteger()

        threadPool?.submit {
            counter.countDown()
            actualTimes.incrementAndGet()
            throw RuntimeException()
        }

        counter.await(1, TimeUnit.MINUTES)
        Thread.sleep(1000)

        assertEquals(0, counter.count, "Should have retried max retry times but didn't in 1 minute.")
        assertEquals(maxRetries, actualTimes.get(), "Shouldn't see any more attempts after max retries, but saw some")
    }

    @Test
    fun `honors suggested delay on error`() {
        val counter = CountDownLatch(maxRetries)
        val now = System.currentTimeMillis()
        val delay = 40L

        threadPool?.submit(object : Retryable {
            override fun run() {
                counter.countDown()
                throw RuntimeException()
            }

            override fun getSuggestedRetryDelayMillis(): Long {
                return delay
            }
        })

        counter.await()

        val actualDelay = System.currentTimeMillis() - now

        assertTrue(actualDelay >= (maxRetries - 1) * delay, "Should have waited between invocations based on the suggested failure delay.")
    }
}
