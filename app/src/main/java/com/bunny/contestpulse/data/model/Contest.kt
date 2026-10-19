package com.bunny.contestpulse.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the platform a contest belongs to.
 */
enum class Platform {
    CODEFORCES,
    CODECHEF,
    LEETCODE
}

/**
 * Represents the phase/status of a contest.
 */
enum class ContestPhase {
    UPCOMING,
    RUNNING,
    FINISHED
}

/**
 * Core data model for a coding contest.
 * Stored locally in Room database.
 */
@Entity(tableName = "contests")
data class Contest(
    @PrimaryKey
    val id: String,                    // Unique ID: "platform_originalId"
    val name: String,                  // Contest name
    val platform: Platform,            // CODEFORCES, CODECHEF, LEETCODE
    val phase: ContestPhase,           // UPCOMING, RUNNING, FINISHED
    val startTimeMillis: Long,         // Start time in epoch milliseconds
    val durationMillis: Long,          // Duration in milliseconds
    val url: String,                   // Direct link to the contest
    val isAlarmEnabled: Boolean = true, // Whether alarm is set for this contest
    val isNotificationEnabled: Boolean = true, // Whether notifications are enabled
    val lastUpdated: Long = System.currentTimeMillis() // When this record was last updated
) {
    /**
     * Returns the end time in epoch milliseconds.
     */
    val endTimeMillis: Long
        get() = startTimeMillis + durationMillis

    /**
     * Returns time remaining until contest starts, in milliseconds.
     * Returns 0 if contest has already started.
     */
    fun timeUntilStart(): Long {
        val remaining = startTimeMillis - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0
    }

    /**
     * Returns true if the contest hasn't started yet.
     */
    fun isUpcoming(): Boolean = System.currentTimeMillis() < startTimeMillis

    /**
     * Returns true if the contest is currently running.
     */
    fun isRunning(): Boolean {
        val now = System.currentTimeMillis()
        return now in startTimeMillis..endTimeMillis
    }
}
