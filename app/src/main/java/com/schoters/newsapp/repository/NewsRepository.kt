package com.schoters.newsapp.repository

import com.schoters.newsapp.model.Article
import com.schoters.newsapp.repository.api.NewsClient
import com.schoters.newsapp.repository.database.ArticleDatabase

class NewsRepository(private val database: ArticleDatabase) {
    suspend fun getBreakingNews(countryCode: String, pageNumber: Int) =
        NewsClient.api.getBreakingNews(country = countryCode, pageNumber = pageNumber)

    suspend fun getSearchNews(q: String, pageNumber: Int) =
        NewsClient.api.getSearchNews(searchQuery = q, pageNumber = pageNumber)

    suspend fun upsert(article: Article) = database.getArticleDao().insert(article = article)

    fun getAllArticles() = database.getArticleDao().getArticles()

    suspend fun delete(article: Article) = database.getArticleDao().deleteArticle(article = article)
}