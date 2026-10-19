package com.bunny.contestpulse.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bunny.contestpulse.data.sync.ContestSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Receiver that reschedules all alarms after device boot.
 * Alarms are lost when the device restarts.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted, rescheduling alarms")

            // Reschedule alarms
            CoroutineScope(Dispatchers.IO).launch {
                AlarmScheduler.scheduleAllAlarms(context)
            }

            // Trigger a sync
            ContestSyncWorker.syncNow(context)
        }
    }
}
