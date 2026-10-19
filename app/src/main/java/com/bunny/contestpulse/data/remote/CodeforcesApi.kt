package com.bunny.contestpulse.data.remote

import com.bunny.contestpulse.data.remote.dto.CodeforcesResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for Codeforces API.
 * Docs: https://codeforces.com/apiHelp
 */
interface CodeforcesApi {
    @GET("contest.list")
    suspend fun getContests(
        @Query("gym") gym: Boolean = false
    ): CodeforcesResponse
}
