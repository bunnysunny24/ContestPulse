package com.bunny.contestpulse.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bunny.contestpulse.data.model.AlarmType

/**
 * BroadcastReceiver that handles alarm triggers.
 * Decides whether to show a notification or launch the full alarm screen.
 */
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val contestId = intent.getStringExtra("contest_id") ?: return
        val contestName = intent.getStringExtra("contest_name") ?: "Contest"
        val contestStart = intent.getLongExtra("contest_start", 0)
        val alarmTypeName = intent.getStringExtra("alarm_type") ?: return

        val alarmType = try {
            AlarmType.valueOf(alarmTypeName)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid alarm type: $alarmTypeName")
            return
        }

        Log.d(TAG, "Alarm received: $contestName (${alarmType.label})")

        when (alarmType) {
            AlarmType.AT_START -> {
                // Launch ringing alarm
                launchRingingAlarm(context, contestId, contestName, contestStart)
            }
            else -> {
                // Show notification reminder
                NotificationHelper.showReminderNotification(
                    context, contestId, contestName, contestStart, alarmType
                )
            }
        }
    }

    private fun launchRingingAlarm(
        context: Context,
        contestId: String,
        contestName: String,
        contestStart: Long
    ) {
        // Start the alarm service (plays sound, shows full-screen notification)
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            putExtra("contest_id", contestId)
            putExtra("contest_name", contestName)
            putExtra("contest_start", contestStart)
        }
        context.startForegroundService(serviceIntent)

        // Also try to launch the alarm activity
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            putExtra("contest_id", contestId)
            putExtra("contest_name", contestName)
            putExtra("contest_start", contestStart)
        }
        context.startActivity(activityIntent)
    }
}
