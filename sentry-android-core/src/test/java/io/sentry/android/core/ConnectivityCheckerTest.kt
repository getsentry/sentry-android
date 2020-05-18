package io.sentry.android.core

import android.content.Context
import android.content.pm.PackageManager.PERMISSION_DENIED
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
import android.net.NetworkInfo
import android.os.Build
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.sentry.android.core.util.ConnectivityChecker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ConnectivityCheckerTest {

    @Test
    fun `When network is active and connected, return true for isConnected`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        val an = mock<NetworkInfo>()
        whenever(cm.activeNetworkInfo).thenReturn(an)
        whenever(an.isConnected).thenReturn(true)
        assertTrue(ConnectivityChecker.isConnected(contextMock, mock())!!)
    }

    @Test
    fun `When network is active but not connected, return false for isConnected`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        val an = mock<NetworkInfo>()
        whenever(cm.activeNetworkInfo).thenReturn(an)
        whenever(an.isConnected).thenReturn(false)
        assertFalse(ConnectivityChecker.isConnected(contextMock, mock())!!)
    }

    @Test
    fun `When there's no permission, return true for isConnected`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        whenever(contextMock.checkPermission(any(), any(), any())).thenReturn(PERMISSION_DENIED)
        assertTrue(ConnectivityChecker.isConnected(contextMock, mock())!!)
    }

    @Test
    fun `When network is not active, return false for isConnected`() {
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
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)
        assertNull(ConnectivityChecker.getConnectionType(mock(), mock(), buildInfo))
    }

    @Test
    fun `When sdkInfoVersion is not min Marshmallow, return null for getConnectionType`() {
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(0)
        assertNull(ConnectivityChecker.getConnectionType(mock(), mock(), buildInfo))
    }

    @Test
    fun `When there's no permission, return null for getConnectionType`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        whenever(contextMock.checkPermission(any(), any(), any())).thenReturn(PERMISSION_DENIED)
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)
        assertNull(ConnectivityChecker.getConnectionType(contextMock, mock(), buildInfo))
    }

    @Test
    fun `When network is not active, return null for getConnectionType`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)
        assertNull(ConnectivityChecker.getConnectionType(contextMock, mock(), buildInfo))
    }

    @Test
    fun `When network capabilities are not available, return null for getConnectionType`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        val net = mock<Network>()
        whenever(cm.activeNetwork).thenReturn(net)
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)
        assertNull(ConnectivityChecker.getConnectionType(contextMock, mock(), buildInfo))
    }

    @Test
    fun `When network capabilities is TRANSPORT_WIFI, return wifi`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        val net = mock<Network>()
        whenever(cm.activeNetwork).thenReturn(net)
        val nc = mock<NetworkCapabilities>()
        whenever(cm.getNetworkCapabilities(any())).thenReturn(nc)
        whenever(nc.hasTransport(eq(TRANSPORT_WIFI))).thenReturn(true)
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)
        assertEquals("wifi", ConnectivityChecker.getConnectionType(contextMock, mock(), buildInfo))
    }

    @Test
    fun `When network capabilities is TRANSPORT_ETHERNET, return wifi`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        val net = mock<Network>()
        whenever(cm.activeNetwork).thenReturn(net)
        val nc = mock<NetworkCapabilities>()
        whenever(cm.getNetworkCapabilities(any())).thenReturn(nc)
        whenever(nc.hasTransport(eq(TRANSPORT_ETHERNET))).thenReturn(true)
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)
        assertEquals("ethernet", ConnectivityChecker.getConnectionType(contextMock, mock(), buildInfo))
    }

    @Test
    fun `When network capabilities has TRANSPORT_CELLULAR, return cellular`() {
        val contextMock = mock<Context>()
        val cm = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(cm)
        val net = mock<Network>()
        whenever(cm.activeNetwork).thenReturn(net)
        val nc = mock<NetworkCapabilities>()
        whenever(cm.getNetworkCapabilities(any())).thenReturn(nc)
        whenever(nc.hasTransport(eq(TRANSPORT_CELLULAR))).thenReturn(true)
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)
        assertEquals("cellular", ConnectivityChecker.getConnectionType(contextMock, mock(), buildInfo))
    }
}
