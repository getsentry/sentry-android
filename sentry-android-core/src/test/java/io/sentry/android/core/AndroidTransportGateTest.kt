package io.sentry.android.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import io.sentry.android.core.util.ConnectivityChecker
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidTransportGateTest {

    private lateinit var context: Context
    private lateinit var transportGate: AndroidTransportGate

    @BeforeTest
    fun `set up`() {
        context = ApplicationProvider.getApplicationContext()
        transportGate = AndroidTransportGate(context, mock())
    }

    @Test
    fun `isSendingAllowed is not null`() {
        assertNotNull(transportGate.isConnected)
    }

    @Test
    fun `isConnected returns true if connection was not found`() {
        assertTrue(transportGate.isConnected(ConnectivityChecker.ConnectionStatus.UNKNOWN))
    }

    @Test
    fun `isConnected returns true if connection is connected`() {
        assertTrue(transportGate.isConnected(ConnectivityChecker.ConnectionStatus.CONNECTED))
    }

    @Test
    fun `isConnected returns false if connection is not connected`() {
        assertFalse(transportGate.isConnected(ConnectivityChecker.ConnectionStatus.NOT_CONNECTED))
    }

    @Test
    fun `isConnected returns false if no permission`() {
        assertTrue(transportGate.isConnected(ConnectivityChecker.ConnectionStatus.NO_PERMISSION))
    }
}
