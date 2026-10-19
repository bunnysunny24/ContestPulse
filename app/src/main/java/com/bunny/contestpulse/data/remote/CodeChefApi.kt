package com.bunny.contestpulse.data.remote

import com.bunny.contestpulse.data.remote.dto.CodeChefApiResponse
import retrofit2.http.GET

interface CodeChefApi {
    @GET("contests/codechef/")
    suspend fun getContests(): CodeChefApiResponse
}
