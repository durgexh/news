package com.newsapp.data

import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface NewsApiService {
    @GET
    suspend fun getRssFeed(@Url url: String): ResponseBody

    companion object {
        private const val BASE_URL = "https://news.google.com/"

        fun create(): NewsApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build()
                .create(NewsApiService::class.java)
        }
    }
}
