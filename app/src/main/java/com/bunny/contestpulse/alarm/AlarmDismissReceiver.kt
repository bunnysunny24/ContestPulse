package com.bunny.contestpulse.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Receiver that handles alarm dismissal from notification action.
 */
class AlarmDismissReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmDismissReceiver", "Dismissing alarm")
        val stopIntent = Intent(context, AlarmService::class.java)
        context.stopService(stopIntent)
    }
}
