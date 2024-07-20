package com.kaitokitaya.easytransfer.httpServer

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.net.Inet4Address
import java.net.InetAddress
import kotlin.contracts.contract

sealed class NetworkStatus {
    data object Disable : NetworkStatus()
    data object PrivateNetwork : NetworkStatus()
    data object MobileTelephony : NetworkStatus()
}


// TODO: Use hilt
class ConnectiveManagerWrapper(private val context: Context) {
    companion object {
        val TAG: String = ConnectiveManagerWrapper::class.java.simpleName
    }

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _isAvailableFlow = MutableStateFlow(false)
    val isAvailable: MutableStateFlow<Boolean> get() = _isAvailableFlow

    init {
        updateNetworkStatus()
    }

    private fun updateNetworkStatus() {
        val status = checkNetworkStatus()
        when (status) {
            NetworkStatus.Disable, NetworkStatus.MobileTelephony -> _isAvailableFlow.update { false }
            NetworkStatus.PrivateNetwork -> _isAvailableFlow.update { true }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            updateNetworkStatus()
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            updateNetworkStatus()
        }

        // ex) Wi-fi => Mobile
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            updateNetworkStatus()
        }

        override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties)
        }
    }

    fun registerCallback() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

    fun getStringIpv4Address(): String? {
        return getIPv4Address()?.hostAddress
    }

    private fun getIPv4Address(): Inet4Address? {
        val network = connectivityManager.activeNetwork ?: return null
        val linkProperties = connectivityManager.getLinkProperties(network)
        linkProperties?.let {
            for (linkAddress in it.linkAddresses) {
                val address: InetAddress = linkAddress.address
                if (address is Inet4Address && !address.isLoopbackAddress) {
                    return address
                }
            }
        }
        return null
    }

    private fun checkNetworkStatus(): NetworkStatus {
        val ipAddress = getIPv4Address()
        ipAddress?.let {
            return if (isPrivateIPv4Address(it)) {
                NetworkStatus.PrivateNetwork
            } else {
                NetworkStatus.MobileTelephony
            }
        }
        return NetworkStatus.Disable
    }

    private fun isPrivateIPv4Address(ip: Inet4Address): Boolean {
        val address = ip.address
        return (address[0].toInt() and 0xFF == 10) ||  // 10.0.0.0/8
                (address[0].toInt() and 0xFF == 172 && (address[1].toInt() and 0xF0 == 16)) ||  // 172.16.0.0/12
                (address[0].toInt() and 0xFF == 192 && address[1].toInt() and 0xFF == 168)  // 192.168.0.0/16
    }

}