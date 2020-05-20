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
import kotlin.test.assertNull

class ConnectivityCheckerTest {

    @Test
    fun `When network is active and connected with permission, return CONNECTED for isConnected`() {
        val contextMock = mock<Context>()
        val connectivityManager = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(connectivityManager)
        val networkInfo = mock<NetworkInfo>()
        whenever(connectivityManager.activeNetworkInfo).thenReturn(networkInfo)
        whenever(networkInfo.isConnected).thenReturn(true)

        assertEquals(ConnectivityChecker.Status.CONNECTED, ConnectivityChecker.getConnectionStatus(contextMock, mock()))
    }

    @Test
    fun `When network is active but not connected with permission, return NOT_CONNECTED for isConnected`() {
        val contextMock = mock<Context>()
        val connectivityManager = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(connectivityManager)
        val networkInfo = mock<NetworkInfo>()
        whenever(connectivityManager.activeNetworkInfo).thenReturn(networkInfo)
        whenever(networkInfo.isConnected).thenReturn(false)

        assertEquals(ConnectivityChecker.Status.NOT_CONNECTED, ConnectivityChecker.getConnectionStatus(contextMock, mock()))
    }

    @Test
    fun `When there's no permission, return NO_PERMISSION for isConnected`() {
        val contextMock = mock<Context>()
        val connectivityManager = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(connectivityManager)
        whenever(contextMock.checkPermission(any(), any(), any())).thenReturn(PERMISSION_DENIED)

        assertEquals(ConnectivityChecker.Status.NO_PERMISSION, ConnectivityChecker.getConnectionStatus(contextMock, mock()))
    }

    @Test
    fun `When network is not active, return NOT_CONNECTED for isConnected`() {
        val contextMock = mock<Context>()
        val connectivityManager = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(connectivityManager)

        assertEquals(ConnectivityChecker.Status.NOT_CONNECTED, ConnectivityChecker.getConnectionStatus(contextMock, mock()))
    }

    @Test
    fun `When ConnectivityManager is not available, return UNKNOWN for isConnected`() {
        assertEquals(ConnectivityChecker.Status.UNKNOWN, ConnectivityChecker.getConnectionStatus(mock(), mock()))
    }

    @Test
    fun `When ConnectivityManager is not available, return null for getConnectionType`() {
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)

        assertNull(ConnectivityChecker.getConnectionStatus(mock(), mock(), buildInfo))
    }

    @Test
    fun `When sdkInfoVersion is not min Marshmallow, return null for getConnectionType`() {
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(0)

        assertNull(ConnectivityChecker.getConnectionStatus(mock(), mock(), buildInfo))
    }

    @Test
    fun `When there's no permission, return null for getConnectionType`() {
        val contextMock = mock<Context>()
        val connectivityManager = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(connectivityManager)
        whenever(contextMock.checkPermission(any(), any(), any())).thenReturn(PERMISSION_DENIED)
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)

        assertNull(ConnectivityChecker.getConnectionStatus(contextMock, mock(), buildInfo))
    }

    @Test
    fun `When network is not active, return null for getConnectionType`() {
        val contextMock = mock<Context>()
        val connectivityManager = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(connectivityManager)
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)

        assertNull(ConnectivityChecker.getConnectionStatus(contextMock, mock(), buildInfo))
    }

    @Test
    fun `When network capabilities are not available, return null for getConnectionType`() {
        val contextMock = mock<Context>()
        val connectivityManager = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(connectivityManager)
        val network = mock<Network>()
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)

        assertNull(ConnectivityChecker.getConnectionStatus(contextMock, mock(), buildInfo))
    }

    @Test
    fun `When network capabilities has TRANSPORT_WIFI, return wifi`() {
        val contextMock = mock<Context>()
        val connectivityManager = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(connectivityManager)
        val network = mock<Network>()
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        val networkCapabilities = mock<NetworkCapabilities>()
        whenever(connectivityManager.getNetworkCapabilities(any())).thenReturn(networkCapabilities)
        whenever(networkCapabilities.hasTransport(eq(TRANSPORT_WIFI))).thenReturn(true)
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)

        assertEquals("wifi", ConnectivityChecker.getConnectionStatus(contextMock, mock(), buildInfo))
    }

    @Test
    fun `When network capabilities has TRANSPORT_ETHERNET, return ethernet`() {
        val contextMock = mock<Context>()
        val connectivityManager = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(connectivityManager)
        val network = mock<Network>()
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        val networkCapabilities = mock<NetworkCapabilities>()
        whenever(connectivityManager.getNetworkCapabilities(any())).thenReturn(networkCapabilities)
        whenever(networkCapabilities.hasTransport(eq(TRANSPORT_ETHERNET))).thenReturn(true)
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)

        assertEquals("ethernet", ConnectivityChecker.getConnectionStatus(contextMock, mock(), buildInfo))
    }

    @Test
    fun `When network capabilities has TRANSPORT_CELLULAR, return cellular`() {
        val contextMock = mock<Context>()
        val connectivityManager = mock<ConnectivityManager>()
        whenever(contextMock.getSystemService(any())).thenReturn(connectivityManager)
        val network = mock<Network>()
        whenever(connectivityManager.activeNetwork).thenReturn(network)
        val networkCapabilities = mock<NetworkCapabilities>()
        whenever(connectivityManager.getNetworkCapabilities(any())).thenReturn(networkCapabilities)
        whenever(networkCapabilities.hasTransport(eq(TRANSPORT_CELLULAR))).thenReturn(true)
        val buildInfo = mock<IBuildInfoProvider>()
        whenever(buildInfo.sdkInfoVersion).thenReturn(Build.VERSION_CODES.M)

        assertEquals("cellular", ConnectivityChecker.getConnectionStatus(contextMock, mock(), buildInfo))
    }
}
