package io.sentry.android.core

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidTransportGateTest {

    private lateinit var context: Context

    @BeforeTest
    fun `set up`() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `isConnected won't throw exception`() {
        val transportGate = AndroidTransportGate(context, mock())
        assertNotNull(transportGate.isSendingAllowed)
    }
}
