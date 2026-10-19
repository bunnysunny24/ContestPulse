package com.bunny.contestpulse.alarm

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bunny.contestpulse.ContestPulseApp
import com.bunny.contestpulse.R

/**
 * Foreground service that plays the alarm sound when a contest starts.
 * Runs as a foreground service so it can play audio reliably.
 */
class AlarmService : Service() {

    companion object {
        private const val TAG = "AlarmService"
        private const val NOTIFICATION_ID = 9999
        private const val MAX_ALARM_DURATION_MS = 5 * 60 * 1000L // 5 minutes max
    }

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val contestId = intent?.getStringExtra("contest_id") ?: ""
        val contestName = intent?.getStringExtra("contest_name") ?: "Contest Starting!"

        Log.d(TAG, "Alarm service started for: $contestName")

        // Show foreground notification with full-screen intent
        startForeground(NOTIFICATION_ID, createAlarmNotification(contestId, contestName))

        // Start playing alarm sound
        startAlarmSound()

        // Start vibration
        startVibration()

        // Auto-stop after max duration
        handler.postDelayed({
            stopAlarm()
        }, MAX_ALARM_DURATION_MS)

        return START_NOT_STICKY
    }

    private fun createAlarmNotification(contestId: String, contestName: String): Notification {
        // Intent to open alarm activity
        val activityIntent = Intent(this, AlarmActivity::class.java).apply {
            putExtra("contest_id", contestId)
            putExtra("contest_name", contestName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Intent to dismiss alarm
        val dismissIntent = Intent(this, AlarmDismissReceiver::class.java)
        val dismissPendingIntent = PendingIntent.getBroadcast(
            this, 0, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, ContestPulseApp.CHANNEL_ALARMS)
            .setContentTitle("⚡ Contest Starting NOW!")
            .setContentText(contestName)
            .setSmallIcon(R.drawable.ic_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(activityPendingIntent, true)
            .setContentIntent(activityPendingIntent)
            .addAction(R.drawable.ic_dismiss, "Dismiss", dismissPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()
    }

    private fun startAlarmSound() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(this@AlarmService, alarmUri)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play alarm sound", e)
        }
    }

    private fun startVibration() {
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
        vibrator?.vibrate(
            VibrationEffect.createWaveform(pattern, 0) // 0 = repeat from index 0
        )
    }

    fun stopAlarm() {
        Log.d(TAG, "Stopping alarm")
        mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null

        vibrator?.cancel()
        vibrator = null

        handler.removeCallbacksAndMessages(null)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
}
