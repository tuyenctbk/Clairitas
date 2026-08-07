package com.example.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BackgroundSyncInfo(
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)

class BackgroundSyncManager(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _syncInfo = MutableStateFlow(
        BackgroundSyncInfo(
            isOnline = isNetworkConnected(),
            lastSyncTimestamp = System.currentTimeMillis()
        )
    )
    val syncInfo: StateFlow<BackgroundSyncInfo> = _syncInfo.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "Device is ONLINE. Triggering background Room sync...")
            _syncInfo.value = _syncInfo.value.copy(isOnline = true, isSyncing = true)
            
            // Trigger WorkManager sync when device comes online
            NewsSyncWorker.triggerImmediateSync(context)

            _syncInfo.value = _syncInfo.value.copy(
                isSyncing = false,
                lastSyncTimestamp = System.currentTimeMillis()
            )
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "Device went OFFLINE.")
            _syncInfo.value = _syncInfo.value.copy(isOnline = false)
        }
    }

    fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
            NewsSyncWorker.schedulePeriodicSync(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering network callback", e)
        }
    }

    fun stopMonitoring() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering network callback", e)
        }
    }

    private fun isNetworkConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    companion object {
        private const val TAG = "BackgroundSyncManager"
    }
}
