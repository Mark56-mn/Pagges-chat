package com.example.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkMonitor(private val context: Context) {

    val isConnected: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(false)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val isConnected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                trySend(isConnected)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
            
        connectivityManager.registerNetworkCallback(request, callback)

        // Send current state immediately
        val currentNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(currentNetwork)
        val currentlyConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        trySend(currentlyConnected)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
