package com.schoters.newsapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.schoters.newsapp.repository.NewsRepository
import com.schoters.newsapp.repository.database.ArticleDatabase
import com.schoters.newsapp.repository.viewmodel.NewsViewModel
import com.schoters.newsapp.repository.viewmodel.NewsViewModelFactory

class MainActivity : AppCompatActivity() {
    lateinit var viewModel: NewsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val newsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelFactory =
            NewsViewModelFactory(newsRepository = newsRepository, app = application)
        viewModel =
            ViewModelProvider(this, viewModelFactory)[NewsViewModel::class.java]
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
        val navHostFragment = findViewById<View>(R.id.nav_host_fragment)
        bottomNavigationView.setupWithNavController(navController = navHostFragment.findNavController())
    }
}