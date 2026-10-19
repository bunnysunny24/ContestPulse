package com.bunny.contestpulse.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CodeChefApiResponse(
    @SerializedName("status") val status: String?,
    @SerializedName("future_contests") val futureContests: List<CodeChefContestItem>?,
    @SerializedName("present_contests") val presentContests: List<CodeChefContestItem>?
)

data class CodeChefContestItem(
    @SerializedName("contest_id") val contestId: String?,
    @SerializedName("contest_code") val contestCode: String?,
    @SerializedName("contest_name") val contestName: String?,
    @SerializedName("contest_start_date_iso") val startDateIso: String?,
    @SerializedName("contest_end_date_iso") val endDateIso: String?,
    @SerializedName("contest_duration") val durationMinutes: String?
)
