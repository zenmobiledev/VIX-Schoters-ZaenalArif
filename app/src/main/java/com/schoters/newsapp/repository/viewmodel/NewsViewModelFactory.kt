package com.schoters.newsapp.repository.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.schoters.newsapp.repository.NewsRepository

class NewsViewModelFactory(val newsRepository: NewsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        NewsViewModel(newsRepository = newsRepository) as T
}