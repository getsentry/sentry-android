package io.sentry.android.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.junit.runner.RunWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    fun `isSendingAllowed won't throw exception`() {
        assertNotNull(transportGate.isSendingAllowed)
    }

    @Test
    fun `isConnected returns true if connection was not found or no permission`() {
        assertTrue(transportGate.isConnected(null))
    }

    @Test
    fun `isConnected returns true if connection is connected`() {
        assertTrue(transportGate.isConnected(true))
    }

    @Test
    fun `isConnected returns false if connection is not connected`() {
        assertFalse(transportGate.isConnected(false))
    }
}
