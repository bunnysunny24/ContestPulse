package com.bunny.contestpulse

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.bunny.contestpulse.data.sync.ContestSyncWorker

/**
 * Application class for ContestPulse.
 * Initializes notification channels and schedules background sync.
 */
class ContestPulseApp : Application() {

    companion object {
        const val CHANNEL_REMINDERS = "contest_reminders"
        const val CHANNEL_ALARMS = "contest_alarms"
    }

    override fun onCreate() {
        super.onCreate()

        // Create notification channels
        createNotificationChannels()

        // Schedule periodic contest sync
        ContestSyncWorker.schedule(this)

        // Also trigger an immediate sync on app start
        ContestSyncWorker.syncNow(this)
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)

        // Reminder channel (normal priority notifications)
        val reminderChannel = NotificationChannel(
            CHANNEL_REMINDERS,
            getString(R.string.channel_reminders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for upcoming contest reminders"
            enableVibration(true)
        }

        // Alarm channel (high priority, with sound)
        val alarmChannel = NotificationChannel(
            CHANNEL_ALARMS,
            getString(R.string.channel_alarms),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alarm notifications when contests are about to start"
            enableVibration(true)
            setBypassDnd(true)
        }

        notificationManager.createNotificationChannel(reminderChannel)
        notificationManager.createNotificationChannel(alarmChannel)
    }
}
