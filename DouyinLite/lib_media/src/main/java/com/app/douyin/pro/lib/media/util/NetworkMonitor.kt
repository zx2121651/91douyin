package com.app.douyin.pro.lib.media.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import com.app.douyin.pro.lib.media.strategy.NetworkQuality
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkQuality = MutableStateFlow(getCurrentQuality())
    val networkQuality: StateFlow<NetworkQuality> = _networkQuality.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateQuality()
        }

        override fun onLost(network: Network) {
            updateQuality()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateQuality()
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    private fun updateQuality() {
        _networkQuality.value = getCurrentQuality()
    }

    private fun getCurrentQuality(): NetworkQuality {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkQuality.NO_NETWORK
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return NetworkQuality.NO_NETWORK

        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkQuality.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // Simplification: assume GOOD for now, could check link speed
                NetworkQuality.GOOD
            }
            else -> NetworkQuality.GOOD
        }
    }

    fun unregister() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}
