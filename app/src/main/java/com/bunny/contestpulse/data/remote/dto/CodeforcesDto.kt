package com.bunny.contestpulse.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Codeforces API response wrapper.
 */
data class CodeforcesResponse(
    @SerializedName("status") val status: String,
    @SerializedName("result") val result: List<CodeforcesContest>
)

/**
 * Represents a single Codeforces contest from the API.
 */
data class CodeforcesContest(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("phase") val phase: String,          // BEFORE, CODING, PENDING_SYSTEM_TEST, SYSTEM_TEST, FINISHED
    @SerializedName("frozen") val frozen: Boolean,
    @SerializedName("durationSeconds") val durationSeconds: Long,
    @SerializedName("startTimeSeconds") val startTimeSeconds: Long?,
    @SerializedName("relativeTimeSeconds") val relativeTimeSeconds: Long?
)
