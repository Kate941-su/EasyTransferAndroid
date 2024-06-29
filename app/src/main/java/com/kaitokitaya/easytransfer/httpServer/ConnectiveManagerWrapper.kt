package com.kaitokitaya.easytransfer.httpServer

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import java.net.Inet4Address
import java.net.InetAddress
import kotlin.contracts.contract

sealed class NetworkStatus {
    data object UnConnected : NetworkStatus()
    data object LocalNetwork : NetworkStatus()
    data object MobileTelephony : NetworkStatus()
}

// TODO: Use hilt
class ConnectiveManagerWrapper(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun getIPAddress(): String? {
        val network = connectivityManager.activeNetwork ?: return null
        val linkProperties = connectivityManager.getLinkProperties(network)
        linkProperties?.let {
            for (linkAddress in it.linkAddresses) {
                val address: InetAddress = linkAddress.address
                if (address is Inet4Address && !address.isLoopbackAddress) {
                    return address.hostAddress
                }
            }
        }
        return null
    }

    fun isNetworkConnected(): NetworkStatus {
        val network = connectivityManager.activeNetwork ?: return NetworkStatus.UnConnected
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return NetworkStatus.UnConnected
        // Real check needed
        if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
            return NetworkStatus.LocalNetwork
        }
        return NetworkStatus.MobileTelephony
    }

}