package io.sentry.android.core

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.sentry.android.core.util.ConnectivityChecker
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConnectivityCheckerTest {

    @Test
    fun `When network is active and connected, return connected`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        val an = mock<NetworkInfo>()
        whenever(cm.activeNetworkInfo).thenReturn(an)
        whenever(an.isConnected).thenReturn(true)
        assertTrue(ConnectivityChecker.isConnected(contextMock, mock())!!)
    }

    @Test
    fun `When network is active but not connected, return not connected`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        val an = mock<NetworkInfo>()
        whenever(cm.activeNetworkInfo).thenReturn(an)
        whenever(an.isConnected).thenReturn(false)
        assertFalse(ConnectivityChecker.isConnected(contextMock, mock())!!)
    }

    @Test
    fun `When there's no permission, return connected`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        whenever(contextMock.checkPermission(any(), any(), any())).thenReturn(PERMISSION_DENIED)
        assertTrue(ConnectivityChecker.isConnected(contextMock, mock())!!)
    }

    @Test
    fun `When network is not active, return not connected`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        assertFalse(ConnectivityChecker.isConnected(contextMock, mock())!!)
    }

    @Test
    fun `When ConnectivityManager is not available, return null for isConnected`() {
        assertNull(ConnectivityChecker.isConnected(mock(), mock()))
    }

    @Test
    fun `When ConnectivityManager is not available, return null for getConnectionType`() {
        assertNull(ConnectivityChecker.getConnectionType(mock(), mock()))
    }
}
