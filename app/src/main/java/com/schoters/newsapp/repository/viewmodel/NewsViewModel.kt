package com.schoters.newsapp.repository.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.*
import android.net.NetworkCapabilities.*
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.schoters.newsapp.model.Article
import com.schoters.newsapp.model.NewsResponse
import com.schoters.newsapp.repository.NewsRepository
import com.schoters.newsapp.utils.AppConnection
import com.schoters.newsapp.utils.Resource
import kotlinx.coroutines.launch
import okio.IOException
import retrofit2.Response

class NewsViewModel(app: Application, val newsRepository: NewsRepository) : AndroidViewModel(app) {

    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    private var breakingPageNumber = 1
    var breakingNewsResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    private var searchPageNumber = 1
    var searchNewsResponse: NewsResponse? = null

    var newSearchQuery: String? = null
    var oldSearchQuery: String? = null

    lateinit var articles: LiveData<PagedList<Article>>

    init {
        getBreakingNews(countryCode = "jp")
    }

    private fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        safeBreakingNewsCall(countryCode = countryCode)
    }

    fun getSearchedNews(queryString: String) = viewModelScope.launch {
        safeSearchNewsCall(searchQuery = queryString)
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                breakingPageNumber++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = result
                } else {
                    val oldArticles = breakingNewsResponse?.articles
                    val newArticles = result.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(data = breakingNewsResponse ?: result)
            }
        }
        return Resource.Error(message = response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>? {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                if (searchNewsResponse == null || newSearchQuery != oldSearchQuery) {
                    searchPageNumber = 1
                    oldSearchQuery = newSearchQuery
                    searchNewsResponse = result
                } else {
                    searchPageNumber++
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = result.articles
                    oldArticles?.addAll(newArticles)
                }
                return Resource.Success(data = searchNewsResponse ?: result)
            }
        }
        return Resource.Error(message = response.message())
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<AppConnection>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(TRANSPORT_WIFI) -> true
                capabilities.hasTransport(TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    TYPE_WIFI -> true
                    TYPE_MOBILE -> true
                    TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

    fun insertArticle(article: Article) =
        viewModelScope.launch { newsRepository.upsert(article = article) }

    fun getBookmarkArticles() = newsRepository.getAllArticles()

    fun deleteArticle(article: Article) =
        viewModelScope.launch { newsRepository.delete(article = article) }

    private suspend fun safeSearchNewsCall(searchQuery: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getSearchNews(
                    q = searchQuery,
                    pageNumber = searchPageNumber
                )
                searchNews.postValue(handleSearchNewsResponse(response = response))
            } else {
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> searchNews.postValue(Resource.Error("Network Failure"))
                else -> searchNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }

    private suspend fun safeBreakingNewsCall(countryCode: String) {
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(
                    countryCode = countryCode,
                    pageNumber = breakingPageNumber
                )
                breakingNews.postValue(handleBreakingNewsResponse(response = response))
            } else {
                breakingNews.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> breakingNews.postValue(Resource.Error("Network Failure"))
                else -> breakingNews.postValue(Resource.Error("Conversion Error"))
            }
        }
    }
}