package com.schoters.newsapp.ui.fragment.features.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.schoters.newsapp.MainActivity
import com.schoters.newsapp.R
import com.schoters.newsapp.ui.fragment.adapter.ArticleAdapter
import com.schoters.newsapp.databinding.FragmentSearchNewsBinding
import com.schoters.newsapp.repository.viewmodel.NewsViewModel
import com.schoters.newsapp.utils.Resource
import com.schoters.newsapp.utils.shareNews
import kotlin.random.Random

class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {
    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: ArticleAdapter
    private var _binding: FragmentSearchNewsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSearchNewsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel
        setupRecycleView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_searchNewsFragment_to_articleFragment,
                bundle
            )
        }

        setSearchNews()

        viewModel.searchNews.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        shimmerFrameLayout.stopShimmer()
                        shimmerFrameLayout.visibility = View.GONE
                    }
                    response.data?.let { news -> newsAdapter.submitList(news.articles.toList()) }
                }

                is Resource.Error -> {
                    binding.apply {
                        shimmerFrameLayout.stopShimmer()
                        shimmerFrameLayout.visibility = View.GONE
                        response.message?.let { message ->
                            Toast.makeText(
                                activity,
                                "An error occurred: $message",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }

                is Resource.Loading -> {
                    binding.apply {
                        shimmerFrameLayout.startShimmer()
                        shimmerFrameLayout.visibility = View.VISIBLE
                    }
                }
            }
        }

        newsAdapter.onSaveClickListener {
            if (it.id == null) {
                it.id = Random.nextInt(0, 1000)
            }
            viewModel.insertArticle(it)
            Snackbar.make(requireView(), "Saved", Snackbar.LENGTH_SHORT).show()
        }

        newsAdapter.onShareNewsClickListener {
            shareNews(context, it)
        }
    }

    private fun setSearchNews() {

        with(binding) {
            searchView.setupWithSearchBar(searchBar)
            searchView
                .editText
                .setOnEditorActionListener { _, _, _ ->
                    val queryText = searchView.text.toString()
                    if (queryText.isEmpty()) {
                        Toast.makeText(
                            requireActivity(),
                            "cannot be empty",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        viewModel.getSearchedNews(queryText)
                        searchView.hide()
                    }
                    false
                }
        }
    }

    private fun setupRecycleView() {
        newsAdapter = ArticleAdapter()
        binding.rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val SEARCH_TIME_DELAY = 500L
    }
}