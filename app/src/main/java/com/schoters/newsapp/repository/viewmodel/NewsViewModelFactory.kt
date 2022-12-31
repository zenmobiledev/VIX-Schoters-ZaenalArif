package com.schoters.newsapp.repository.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.schoters.newsapp.repository.NewsRepository

class NewsViewModelFactory(val app: Application, val newsRepository: NewsRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        NewsViewModel(newsRepository = newsRepository, app = app) as T
}