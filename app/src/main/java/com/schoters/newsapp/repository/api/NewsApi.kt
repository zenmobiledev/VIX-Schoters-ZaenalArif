package com.schoters.newsapp.repository.api

import com.schoters.newsapp.BuildConfig
import com.schoters.newsapp.model.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("v2/top-headlines")
    suspend fun getBreakingNews(
        @Query("country") country: String = "jp",
        @Query("page") pageNumber: Int,
        @Query("apiKey") apiKey: String = BuildConfig.NEWSAPP_TOKEN
    ): Response<NewsResponse>

    @GET("v2/everything")
    suspend fun getSearchNews(
        @Query("q") searchQuery: String,
        @Query("page") pageNumber: Int,
        @Query("apiKey") apiKey: String = BuildConfig.NEWSAPP_TOKEN
    ): Response<NewsResponse>
}