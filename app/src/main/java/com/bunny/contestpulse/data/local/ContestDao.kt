package com.bunny.contestpulse.data.local

import androidx.room.*
import com.bunny.contestpulse.data.model.Contest
import com.bunny.contestpulse.data.model.Platform
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Contest entities.
 */
@Dao
interface ContestDao {

    /**
     * Get all upcoming contests ordered by start time.
     */
    @Query("SELECT * FROM contests WHERE startTimeMillis > :currentTime ORDER BY startTimeMillis ASC")
    fun getUpcomingContests(currentTime: Long = System.currentTimeMillis()): Flow<List<Contest>>

    /**
     * Get upcoming contests for a specific platform.
     */
    @Query("SELECT * FROM contests WHERE platform = :platform AND startTimeMillis > :currentTime ORDER BY startTimeMillis ASC")
    fun getUpcomingContestsByPlatform(
        platform: Platform,
        currentTime: Long = System.currentTimeMillis()
    ): Flow<List<Contest>>

    /**
     * Get the next upcoming contest for each platform.
     */
    @Query("""
        SELECT * FROM contests 
        WHERE startTimeMillis > :currentTime 
        ORDER BY startTimeMillis ASC
    """)
    suspend fun getAllUpcomingContestsList(currentTime: Long = System.currentTimeMillis()): List<Contest>

    /**
     * Get a specific contest by ID.
     */
    @Query("SELECT * FROM contests WHERE id = :contestId")
    suspend fun getContestById(contestId: String): Contest?

    /**
     * Get all contests with alarms enabled.
     */
    @Query("SELECT * FROM contests WHERE isAlarmEnabled = 1 AND startTimeMillis > :currentTime")
    suspend fun getContestsWithAlarms(currentTime: Long = System.currentTimeMillis()): List<Contest>

    /**
     * Insert or update contests.
     */
    @Upsert
    suspend fun upsertContests(contests: List<Contest>)

    /**
     * Update alarm enabled status for a contest.
     */
    @Query("UPDATE contests SET isAlarmEnabled = :enabled WHERE id = :contestId")
    suspend fun updateAlarmEnabled(contestId: String, enabled: Boolean)

    /**
     * Update notification enabled status for a contest.
     */
    @Query("UPDATE contests SET isNotificationEnabled = :enabled WHERE id = :contestId")
    suspend fun updateNotificationEnabled(contestId: String, enabled: Boolean)

    /**
     * Delete old finished contests (cleanup).
     */
    @Query("DELETE FROM contests WHERE (startTimeMillis + durationMillis) < :cutoffTime")
    suspend fun deleteOldContests(cutoffTime: Long)

    /**
     * Get all contests (for widget).
     */
    @Query("SELECT * FROM contests WHERE startTimeMillis > :currentTime ORDER BY startTimeMillis ASC LIMIT :limit")
    suspend fun getNextContests(currentTime: Long = System.currentTimeMillis(), limit: Int = 6): List<Contest>
}
