package com.bunny.contestpulse.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.bunny.contestpulse.data.model.AlarmType
import com.bunny.contestpulse.data.repository.ContestRepository

/**
 * Schedules and manages alarms for upcoming contests.
 * Uses AlarmManager for precise alarm scheduling.
 */
object AlarmScheduler {

    private const val TAG = "AlarmScheduler"

    /**
     * Schedule alarms for all enabled contests.
     */
    suspend fun scheduleAllAlarms(context: Context) {
        val repository = ContestRepository.getInstance(context)
        val contests = repository.getContestsWithAlarms()

        Log.d(TAG, "Scheduling alarms for ${contests.size} contests")

        contests.forEach { contest ->
            AlarmType.entries.forEach { alarmType ->
                scheduleAlarm(context, contest.id, contest.name, contest.startTimeMillis, alarmType)
            }
        }
    }

    /**
     * Schedule a single alarm for a specific contest and alarm type.
     */
    fun scheduleAlarm(
        context: Context,
        contestId: String,
        contestName: String,
        contestStartMillis: Long,
        alarmType: AlarmType
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = alarmType.getTriggerTimeMillis(contestStartMillis)
        val now = System.currentTimeMillis()

        // Don't schedule alarms in the past
        if (triggerTime <= now) {
            Log.d(TAG, "Skipping past alarm: $contestName (${alarmType.label})")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = "com.bunny.contestpulse.CONTEST_ALARM"
            putExtra("contest_id", contestId)
            putExtra("contest_name", contestName)
            putExtra("contest_start", contestStartMillis)
            putExtra("alarm_type", alarmType.name)
        }

        val requestCode = generateRequestCode(contestId, alarmType)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(
                        AlarmManager.AlarmClockInfo(triggerTime, null),
                        pendingIntent
                    )
                } else {
                    // Fallback: use inexact alarm
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerTime, null),
                    pendingIntent
                )
            }

            Log.d(TAG, "Alarm scheduled: $contestName (${alarmType.label}) at $triggerTime")
        } catch (e: SecurityException) {
            Log.e(TAG, "Cannot schedule exact alarm: ${e.message}")
            // Fallback to inexact alarm
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    /**
     * Cancel all alarms for a specific contest.
     */
    fun cancelAlarms(context: Context, contestId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        AlarmType.entries.forEach { alarmType ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val requestCode = generateRequestCode(contestId, alarmType)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }

        Log.d(TAG, "Cancelled alarms for contest: $contestId")
    }

    /**
     * Generate a unique request code for each contest + alarm type combination.
     */
    private fun generateRequestCode(contestId: String, alarmType: AlarmType): Int {
        return (contestId.hashCode() * 31 + alarmType.ordinal)
    }
}
