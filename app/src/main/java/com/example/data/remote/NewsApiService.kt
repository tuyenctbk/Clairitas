package com.example.data.remote

import com.example.data.remote.model.NewsResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface to fetch news articles from a public news API endpoint.
 * Uses Moshi for JSON parsing.
 */
interface NewsApiService {

    /**
     * Fetches top headline news articles.
     *
     * @param apiKey API key for authentication.
     * @param country 2-letter ISO 3166-1 code of the country (e.g. "us").
     * @param category News category (e.g. "business", "entertainment", "general", "health", "science", "sports", "technology").
     * @param query Search keywords or phrase to filter articles.
     * @param pageSize Number of results per page (max 100).
     * @param page Page number for pagination.
     */
    @GET("v2/top-headlines")
    suspend fun getTopHeadlines(
        @Query("apiKey") apiKey: String? = null,
        @Query("country") country: String? = "us",
        @Query("category") category: String? = null,
        @Query("q") query: String? = null,
        @Query("pageSize") pageSize: Int? = 20,
        @Query("page") page: Int? = 1
    ): Response<NewsResponseDto>

    /**
     * Searches through all articles from thousands of news sources and blogs.
     *
     * @param query Keywords or phrases to search for in article title and body.
     * @param apiKey API key for authentication.
     * @param language 2-letter ISO-639-1 code of the language (e.g. "en").
     * @param sortBy Order to sort articles (e.g. "relevance", "popularity", "publishedAt").
     * @param pageSize Number of results per page (max 100).
     * @param page Page number for pagination.
     */
    @GET("v2/everything")
    suspend fun getEverything(
        @Query("q") query: String,
        @Query("apiKey") apiKey: String? = null,
        @Query("language") language: String? = "en",
        @Query("sortBy") sortBy: String? = "publishedAt",
        @Query("pageSize") pageSize: Int? = 20,
        @Query("page") page: Int? = 1
    ): Response<NewsResponseDto>
}
