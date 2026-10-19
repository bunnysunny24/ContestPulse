package com.bunny.contestpulse.alarm

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.bunny.contestpulse.ContestPulseApp
import com.bunny.contestpulse.R
import com.bunny.contestpulse.data.model.AlarmType
import com.bunny.contestpulse.ui.MainActivity
import java.text.SimpleDateFormat
import java.util.*

/**
 * Helper for creating and showing contest reminder notifications.
 */
object NotificationHelper {

    private const val TAG = "NotificationHelper"

    /**
     * Show a reminder notification before a contest starts.
     */
    fun showReminderNotification(
        context: Context,
        contestId: String,
        contestName: String,
        contestStartMillis: Long,
        alarmType: AlarmType
    ) {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val startTime = timeFormat.format(Date(contestStartMillis))

        val title = when (alarmType) {
            AlarmType.THIRTY_MINUTES -> "🔔 Contest in 30 minutes!"
            AlarmType.FIVE_MINUTES -> "⚠️ Contest in 5 minutes!"
            AlarmType.AT_START -> "⚡ Contest starting now!"
        }

        val text = "$contestName starts at $startTime"

        // Intent to open the app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, contestId.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, ContestPulseApp.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .build()

        if (ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context)
                .notify(contestId.hashCode() + alarmType.ordinal, notification)
        } else {
            Log.w(TAG, "Notification permission not granted")
        }
    }
}
