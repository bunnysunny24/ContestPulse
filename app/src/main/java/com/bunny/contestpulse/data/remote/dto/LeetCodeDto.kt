package com.bunny.contestpulse.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * GraphQL request body for LeetCode API.
 */
data class LeetCodeGraphQLRequest(
    @SerializedName("query") val query: String,
    @SerializedName("variables") val variables: Map<String, Any> = emptyMap(),
    @SerializedName("operationName") val operationName: String? = null
)

/**
 * LeetCode GraphQL response for upcoming contests.
 */
data class LeetCodeGraphQLResponse(
    @SerializedName("data") val data: LeetCodeData?
)

data class LeetCodeData(
    @SerializedName("upcomingContests") val upcomingContests: List<LeetCodeContest>?
)

/**
 * Represents a single LeetCode contest.
 */
data class LeetCodeContest(
    @SerializedName("title") val title: String,
    @SerializedName("titleSlug") val titleSlug: String?,
    @SerializedName("startTime") val startTime: Long,       // Unix epoch seconds
    @SerializedName("duration") val duration: Long,          // Duration in seconds
    @SerializedName("cardImg") val cardImg: String?
)
