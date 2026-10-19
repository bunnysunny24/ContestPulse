package com.bunny.contestpulse.data.model

/**
 * Types of alarms/reminders that can be set for contests.
 */
enum class AlarmType(val minutesBefore: Int, val label: String) {
    THIRTY_MINUTES(30, "30 minutes before"),
    FIVE_MINUTES(5, "5 minutes before"),
    AT_START(0, "At contest start");

    /**
     * Calculate the alarm trigger time based on contest start time.
     */
    fun getTriggerTimeMillis(contestStartMillis: Long): Long {
        return contestStartMillis - (minutesBefore * 60 * 1000L)
    }
}
