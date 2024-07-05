package com.schoters.newsapp.repository.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_ETHERNET
import android.net.NetworkCapabilities.TRANSPORT_WIFI
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

class NewsViewModel(
    app: Application,
    private val newsRepository: NewsRepository
) : AndroidViewModel(app) {

    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    private var breakingPageNumber = 1
    private var breakingNewsResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    private var searchPageNumber = 1
    private var searchNewsResponse: NewsResponse? = null

    private var newSearchQuery: String? = null
    private var oldSearchQuery: String? = null

    lateinit var articles: LiveData<PagedList<Article>>

    init {
        getBreakingNews("jp")
    }

    private fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        safeBreakingNewsCall(countryCode)
    }

    fun getSearchedNews(queryString: String) {
        newSearchQuery = queryString
        viewModelScope.launch {
            safeSearchNewsCall(queryString)
        }
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        response.body()?.let { result ->
            breakingPageNumber++
            breakingNewsResponse = breakingNewsResponse?.apply {
                articles.addAll(result.articles)
            } ?: result
            return Resource.Success(breakingNewsResponse ?: result)
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>? {
        response.body()?.let { result ->
            if (searchNewsResponse == null || newSearchQuery != oldSearchQuery) {
                searchPageNumber = 1
                oldSearchQuery = newSearchQuery
                searchNewsResponse = result
            } else {
                searchPageNumber++
                searchNewsResponse?.articles?.addAll(result.articles)
            }
            return Resource.Success(searchNewsResponse ?: result)
        }
        return Resource.Error(response.message())
    }

    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<AppConnection>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return capabilities.run {
                hasTransport(TRANSPORT_WIFI) || hasTransport(TRANSPORT_CELLULAR) || hasTransport(
                    TRANSPORT_ETHERNET
                )
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true
        }
    }

    fun insertArticle(article: Article) = viewModelScope.launch {
        newsRepository.upsert(article)
    }

    fun getBookmarkArticles() = newsRepository.getAllArticles()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.delete(article)
    }

    private suspend fun safeSearchNewsCall(searchQuery: String) {
        searchNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getSearchNews(searchQuery, searchPageNumber)
                searchNews.postValue(handleSearchNewsResponse(response))
            } else {
                searchNews.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            searchNews.postValue(
                when (t) {
                    is IOException -> Resource.Error("Network Failure")
                    else -> Resource.Error("Conversion Error")
                }
            )
        }
    }

    private suspend fun safeBreakingNewsCall(countryCode: String) {
        breakingNews.postValue(Resource.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getBreakingNews(countryCode, breakingPageNumber)
                breakingNews.postValue(handleBreakingNewsResponse(response))
            } else {
                breakingNews.postValue(Resource.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            breakingNews.postValue(
                when (t) {
                    is IOException -> Resource.Error("Network Failure")
                    else -> Resource.Error("Conversion Error")
                }
            )
        }
    }
}
