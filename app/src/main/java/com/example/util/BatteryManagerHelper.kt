package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BatteryStatus(
    val percentage: Int = 100,
    val isCharging: Boolean = false,
    val isLowPowerRecommended: Boolean = false
)

class BatteryManagerHelper(private val context: Context) {

    private val prefs = context.getSharedPreferences("sift_prefs", Context.MODE_PRIVATE)

    private val _batteryStatus = MutableStateFlow(getCurrentStatus())
    val batteryStatus: StateFlow<BatteryStatus> = _batteryStatus.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                val pct = if (level >= 0 && scale > 0) ((level * 100) / scale.toFloat()).toInt() else 100
                val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val lowPower = pct <= 15 && !charging

                _batteryStatus.value = BatteryStatus(
                    percentage = pct,
                    isCharging = charging,
                    isLowPowerRecommended = lowPower
                )

                // Dynamically update WorkManager constraints to suspend/restore background sync
                com.example.service.NewsSyncWorker.updateWorkManagerConstraints(context, lowPower)

                // Auto-trigger low power mode preference if critical
                if (lowPower) {
                    val currentSetting = prefs.getBoolean("is_low_power_mode", false)
                    if (!currentSetting) {
                        prefs.edit().putBoolean("is_low_power_mode", true).apply()
                        Log.w(TAG, "Battery critical ($pct%). Automatically enabled Low Power Mode for background syncs.")
                    }
                }
            }
        }
    }

    fun startMonitoring() {
        try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(batteryReceiver, filter)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register battery receiver", e)
        }
    }

    fun stopMonitoring() {
        try {
            context.unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unregister battery receiver", e)
        }
    }

    private fun getCurrentStatus(): BatteryStatus {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 100
        val isCharging = bm?.isCharging ?: false
        val lowPower = level <= 15 && !isCharging

        return BatteryStatus(
            percentage = level,
            isCharging = isCharging,
            isLowPowerRecommended = lowPower
        )
    }

    companion object {
        private const val TAG = "BatteryManagerHelper"
    }
}
