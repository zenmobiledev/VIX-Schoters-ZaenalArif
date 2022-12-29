package com.schoters.newsapp.repository.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagedList
import com.schoters.newsapp.model.Article
import com.schoters.newsapp.model.NewsResponse
import com.schoters.newsapp.repository.NewsRepository
import com.schoters.newsapp.utils.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(val newsRepository: NewsRepository) : ViewModel() {

    val breakingNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var breakingPageNumber = 1
    var breakingNewsResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchPageNumber = 1
    var searchNewsResponse: NewsResponse? = null

    lateinit var articles: LiveData<PagedList<Article>>

    init {
        getBreakingNews(countryCode = "jp")
    }

    private fun getBreakingNews(countryCode: String) = viewModelScope.launch {
        breakingNews.postValue(Resource.Loading())

        val response = newsRepository.getBreakingNews(
            countryCode = countryCode,
            pageNumber = breakingPageNumber
        )

        breakingNews.postValue(handleBreakingNewsResponse(response = response))
    }

    private fun handleBreakingNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                breakingPageNumber++
                if (breakingNewsResponse == null) {
                    breakingNewsResponse = result
                } else {
                    val oldArticle = breakingNewsResponse?.articles
                    val newArticle = result.articles
                    oldArticle?.addAll(elements = newArticle)
                }
                return Resource.Success(data = breakingNewsResponse ?: result)
            }
        }
        return Resource.Error(message = response.message())
    }

    fun getSearchedNews(queryString: String) = viewModelScope.launch {
        searchNews.postValue(Resource.Loading())

        val searchNewsResponse =
            newsRepository.getSearchNews(q = queryString, pageNumber = searchPageNumber)
        searchNews.postValue(handleBreakingNewsResponse(searchNewsResponse))
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse>? {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                searchPageNumber++
                if (searchNewsResponse == null) {
                    searchNewsResponse = result
                } else {
                    val oldArticles = searchNewsResponse?.articles
                    val newArticles = result.articles
                    oldArticles?.addAll(elements = newArticles)
                }
                return Resource.Success(searchNewsResponse ?: result)
            }
        }
        return Resource.Error(message = response.message())
    }

    fun insertArticle(article: Article) =
        viewModelScope.launch { newsRepository.upsert(article = article) }

    fun deleteArticle(article: Article) =
        viewModelScope.launch { newsRepository.delete(article = article) }

    fun getBookmarkArticles() = newsRepository.getAllArticles()

    fun getBreakingNews(): LiveData<PagedList<Article>> = articles
}