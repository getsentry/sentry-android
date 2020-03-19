package io.sentry.core.transport

import java.util.concurrent.CountDownLatch
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class RetryingThreadPoolExecutorTest {
    private val maxQueueSize = 5
    private var threadPool: RetryingThreadPoolExecutor? = null

    @BeforeTest
    fun setup() {
        val threadFactory = ThreadFactory { r ->
            val t = Thread(r, "RetryingScheduledThreadPoolExecutorTestThread")
            t.isDaemon = true
            t
        }

        // make sure we have enough threads to handle more than the maximum number of enqueued operations
        // in reality this would not be a problem but the test code needs to synchronize the main thread
        // with a number of jobs. If there weren't enough threads, the main thread could block indefinitely
        // because there wouldn't be enough worker threads to handle all jobs in the queue (because the test
        // code blocks the worker threads).
        threadPool = RetryingThreadPoolExecutor(maxQueueSize + 1, maxQueueSize, threadFactory, DiscardPolicy())
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
    fun `do not retry failed tasks`() {
        val counter = CountDownLatch(1)
        val actualTimes = AtomicInteger()

        threadPool?.submit {
            counter.countDown()
            actualTimes.incrementAndGet()
            throw RuntimeException()
        }

        counter.await()
        Thread.sleep(1000)

        assertEquals(1, actualTimes.get(), "Shouldn't see any more attempts, but saw some")
    }

    @Test
    fun `limits the queue size`() {
        // using this we're waiting for the submitted jobs to be unblocked
        val jobBlocker = Object()

        // this is used to wait on the main thread until all the jobs are started
        val sync = CountDownLatch(maxQueueSize)

        // this is used to block the main thread until at least 1 of the jobs has finished
        val atLeastOneFinished = CountDownLatch(1)

        val futures = (1..maxQueueSize).map {
            threadPool?.submit {
                sync.countDown()

                // using the primitive notify/wait enables us to wake up the jobs 1 by 1.
                synchronized(jobBlocker) { jobBlocker.wait() }

                // signal that we're finished
                atLeastOneFinished.countDown()
            }
        }

        // wait for the jobs to start
        sync.await()

        futures.forEach {
            assertNotNull(it)
            assertFalse(it.isCancelled, "No task below the max queue size should be cancelled.")
        }

        var f = threadPool?.submit { synchronized(jobBlocker) { jobBlocker.wait() } }
        assertTrue(f != null && f.isCancelled, "A task above the queue size should have been cancelled.")

        // wake up a single job and wait on the main thread for that to finish
        synchronized(jobBlocker) { jobBlocker.notify() }
        atLeastOneFinished.await()

        var waitCount = 0
        // wait for the thread pool to realize that the job indeed finished
        while (threadPool?.completedTaskCount == 0L) {
            waitCount++
            if (waitCount == 10) {
                fail()
            }
            Thread.sleep(100)
        }

        // now try to test that the above actually made room in the queue again
        val jobBlocker2 = CountDownLatch(1)
        val sync2 = CountDownLatch(1)

        f = threadPool?.submit { sync2.countDown(); jobBlocker2.await() }
        assertFalse(f != null && f.isCancelled, "A task should be successfully enqueued after making a place in the queue")
        sync2.await()

        synchronized(jobBlocker) { jobBlocker.notifyAll() }
        jobBlocker2.countDown()
    }
}
