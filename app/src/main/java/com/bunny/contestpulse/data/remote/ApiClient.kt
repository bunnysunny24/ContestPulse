package com.bunny.contestpulse.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Provides configured Retrofit instances for each API.
 */
object ApiClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Codeforces API client.
     */
    val codeforcesApi: CodeforcesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://codeforces.com/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CodeforcesApi::class.java)
    }

    /**
     * LeetCode GraphQL API client.
     */
    val leetCodeApi: LeetCodeApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://leetcode.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LeetCodeApi::class.java)
    }

    /**
     * CodeChef contests API client (using Kontests API).
     */
    val codeChefApi: CodeChefApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://competeapi.vercel.app/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CodeChefApi::class.java)
    }
}
