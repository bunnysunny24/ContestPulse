package com.bunny.contestpulse.data.remote

import com.bunny.contestpulse.data.remote.dto.LeetCodeGraphQLRequest
import com.bunny.contestpulse.data.remote.dto.LeetCodeGraphQLResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit interface for LeetCode GraphQL API.
 */
interface LeetCodeApi {
    @POST("graphql")
    suspend fun query(
        @Body request: LeetCodeGraphQLRequest
    ): LeetCodeGraphQLResponse
}
