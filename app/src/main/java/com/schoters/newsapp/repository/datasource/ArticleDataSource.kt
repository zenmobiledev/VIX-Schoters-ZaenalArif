package com.schoters.newsapp.repository.datasource

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.paging.PageKeyedDataSource
import com.bumptech.glide.load.engine.Resource
import com.schoters.newsapp.BuildConfig
import com.schoters.newsapp.model.Article
import com.schoters.newsapp.model.NewsResponse
import com.schoters.newsapp.repository.api.NewsClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class ArticleDataSource(val scope: CoroutineScope) : PageKeyedDataSource<Int, Article>() {

    val breakingNews: MutableLiveData<MutableList<Article>> = MutableLiveData()
    var breakingPageNumber = 1
    var breakingNewsResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchPageNumber = 1
    var searchNewsResponse: NewsResponse? = null

    override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Article>) {
        try {
            scope.launch {
                val response = NewsClient.api.getBreakingNews(
                    "jp",
                    params.requestedLoadSize,
                    BuildConfig.NEWSAPP_TOKEN
                )
                when {
                    response.isSuccessful -> {
                        response.body()?.articles?.let {
                            callback.onResult(it, params.key + 1)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
    }

    override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Article>) {
        TODO("Not yet implemented")
    }

    override fun loadInitial(
        params: LoadInitialParams<Int>,
        callback: LoadInitialCallback<Int, Article>
    ) {
        scope.launch {
            try {
                val response = NewsClient.api.getBreakingNews("jp", 1, BuildConfig.NEWSAPP_TOKEN)
                when {
                    response.isSuccessful -> {
                        response.body()?.articles?.let {
                            breakingNews.postValue(it)
                            callback.onResult(it, null, 2)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.message.toString())
            }
        }
    }

    companion object {
        private const val TAG = "DataSource"
    }
}