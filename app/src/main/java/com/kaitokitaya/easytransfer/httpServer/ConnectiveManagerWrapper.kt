package com.kaitokitaya.easytransfer.httpServer

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

sealed class NetworkStatus {
    data object UnConnected : NetworkStatus()
    data object LocalNetwork : NetworkStatus()
    data object MobileTelephony : NetworkStatus()
}


class ConnectiveManagerWrapper(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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