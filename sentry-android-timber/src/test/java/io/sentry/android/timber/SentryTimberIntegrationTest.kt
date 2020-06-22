package io.sentry.android.timber

import com.nhaarman.mockitokotlin2.mock
import io.sentry.core.IHub
import io.sentry.core.SentryOptions
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import timber.log.Timber

class SentryTimberIntegrationTest {

    private class Fixture {
        val hub = mock<IHub>()
        val options = SentryOptions()

        fun getSut(): SentryTimberIntegration {
            return SentryTimberIntegration()
        }
    }
    private val fixture = Fixture()

    @BeforeTest
    fun beforeTest() {
        Timber.uprootAll()
    }

    @Test
    fun `Integrations plants a tree into Timber on register`() {
        val sut = fixture.getSut()
        sut.register(fixture.hub, fixture.options)

        assertEquals(1, Timber.treeCount())
    }

    @Test
    fun `Integrations removes a tree from Timber on close integration`() {
        val sut = fixture.getSut()
        sut.register(fixture.hub, fixture.options)

        assertEquals(1, Timber.treeCount())

        sut.close()
        assertEquals(0, Timber.treeCount())
    }

    @Test
    fun `Integrations do not throw if close is called before register`() {
        val sut = fixture.getSut()
        sut.close()

        assertEquals(0, Timber.treeCount())
    }
}
