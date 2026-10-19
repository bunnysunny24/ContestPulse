package com.bunny.contestpulse.data.repository

import android.content.Context
import android.util.Log
import com.bunny.contestpulse.data.local.ContestDatabase
import com.bunny.contestpulse.data.model.Contest
import com.bunny.contestpulse.data.model.ContestPhase
import com.bunny.contestpulse.data.model.Platform
import com.bunny.contestpulse.data.remote.ApiClient
import com.bunny.contestpulse.data.remote.ContestMapper
import com.bunny.contestpulse.data.remote.dto.LeetCodeGraphQLRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository that manages contest data from multiple sources.
 * Single source of truth backed by Room database.
 */
class ContestRepository(private val context: Context) {

    private val dao = ContestDatabase.getInstance(context).contestDao()
    private val codeforcesApi = ApiClient.codeforcesApi
    private val leetCodeApi = ApiClient.leetCodeApi
    private val codeChefApi = ApiClient.codeChefApi

    companion object {
        private const val TAG = "ContestRepository"

        @Volatile
        private var INSTANCE: ContestRepository? = null

        fun getInstance(context: Context): ContestRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ContestRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    /**
     * Observe all upcoming contests as a Flow.
     */
    fun getUpcomingContests(): Flow<List<Contest>> {
        return dao.getUpcomingContests()
    }

    /**
     * Observe upcoming contests for a specific platform.
     */
    fun getUpcomingContestsByPlatform(platform: Platform): Flow<List<Contest>> {
        return dao.getUpcomingContestsByPlatform(platform)
    }

    /**
     * Get contests for the widget (non-reactive).
     */
    suspend fun getNextContestsForWidget(limit: Int = 6): List<Contest> {
        return dao.getNextContests(limit = limit)
    }

    /**
     * Get all contests with alarms enabled.
     */
    suspend fun getContestsWithAlarms(): List<Contest> {
        return dao.getContestsWithAlarms()
    }

    /**
     * Toggle alarm for a contest.
     */
    suspend fun toggleAlarm(contestId: String, enabled: Boolean) {
        dao.updateAlarmEnabled(contestId, enabled)
    }

    /**
     * Toggle notification for a contest.
     */
    suspend fun toggleNotification(contestId: String, enabled: Boolean) {
        dao.updateNotificationEnabled(contestId, enabled)
    }

    /**
     * Fetch contests from all platforms and save to database.
     * Returns true if successful (at least one platform succeeded).
     */
    suspend fun refreshContests(): Boolean = withContext(Dispatchers.IO) {
        var anySuccess = false

        coroutineScope {
            val cfJob = async { fetchCodeforces() }
            val lcJob = async { fetchLeetCode() }
            val ccJob = async { fetchCodeChef() }

            if (cfJob.await()) anySuccess = true
            if (lcJob.await()) anySuccess = true
            if (ccJob.await()) anySuccess = true
        }

        // Cleanup old finished contests (older than 7 days)
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        dao.deleteOldContests(sevenDaysAgo)

        if (anySuccess) {
            com.bunny.contestpulse.widget.WidgetUpdater.updateWidgets(context)
        }

        anySuccess
    }

    /**
     * Fetch Codeforces contests.
     */
    private suspend fun fetchCodeforces(): Boolean {
        return try {
            val response = codeforcesApi.getContests()
            if (response.status == "OK") {
                val contests = response.result
                    .filter { it.phase == "BEFORE" || it.phase == "CODING" }
                    .map { ContestMapper.fromCodeforces(it) }

                if (contests.isNotEmpty()) {
                    // Preserve alarm settings from existing records
                    val updatedContests = contests.map { newContest ->
                        val existing = dao.getContestById(newContest.id)
                        if (existing != null) {
                            newContest.copy(
                                isAlarmEnabled = existing.isAlarmEnabled,
                                isNotificationEnabled = existing.isNotificationEnabled
                            )
                        } else {
                            newContest
                        }
                    }
                    dao.upsertContests(updatedContests)
                }
                Log.d(TAG, "Codeforces: fetched ${contests.size} contests")
                true
            } else {
                Log.w(TAG, "Codeforces API returned status: ${response.status}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch Codeforces contests", e)
            false
        }
    }

    /**
     * Fetch LeetCode contests via GraphQL.
     */
    private suspend fun fetchLeetCode(): Boolean {
        return try {
            val query = """
                query upcomingContests {
                    upcomingContests {
                        title
                        titleSlug
                        startTime
                        duration
                        cardImg
                    }
                }
            """.trimIndent()

            val request = LeetCodeGraphQLRequest(
                query = query,
                operationName = "upcomingContests"
            )

            val response = leetCodeApi.query(request)
            val lcContests = response.data?.upcomingContests

            if (lcContests != null) {
                val contests = lcContests.map { ContestMapper.fromLeetCode(it) }
                if (contests.isNotEmpty()) {
                    val updatedContests = contests.map { newContest ->
                        val existing = dao.getContestById(newContest.id)
                        if (existing != null) {
                            newContest.copy(
                                isAlarmEnabled = existing.isAlarmEnabled,
                                isNotificationEnabled = existing.isNotificationEnabled
                            )
                        } else {
                            newContest
                        }
                    }
                    dao.upsertContests(updatedContests)
                }
                Log.d(TAG, "LeetCode: fetched ${contests.size} contests")
                true
            } else {
                Log.w(TAG, "LeetCode API returned null data")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch LeetCode contests", e)
            false
        }
    }

    /**
     * Fetch CodeChef contests via Kontests API.
     */
    private suspend fun fetchCodeChef(): Boolean {
        return try {
            val response = codeChefApi.getContests()
            val allContests = (response.futureContests.orEmpty() + response.presentContests.orEmpty())
            val contests = allContests.mapNotNull { ContestMapper.fromCodeChef(it) }
                .filter { it.phase != ContestPhase.FINISHED }

            if (contests.isNotEmpty()) {
                val updatedContests = contests.map { newContest ->
                    val existing = dao.getContestById(newContest.id)
                    if (existing != null) {
                        newContest.copy(
                            isAlarmEnabled = existing.isAlarmEnabled,
                            isNotificationEnabled = existing.isNotificationEnabled
                        )
                    } else {
                        newContest
                    }
                }
                dao.upsertContests(updatedContests)
            }
            Log.d(TAG, "CodeChef: fetched ${contests.size} contests")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch CodeChef contests", e)
            false
        }
    }
}
