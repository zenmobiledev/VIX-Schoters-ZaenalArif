package com.schoters.newsapp.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.schoters.newsapp.MainActivity
import com.schoters.newsapp.R
import com.schoters.newsapp.adapter.ArticleAdapter
import com.schoters.newsapp.databinding.FragmentSearchNewsBinding
import com.schoters.newsapp.repository.viewmodel.NewsViewModel
import com.schoters.newsapp.utils.Resource
import com.schoters.newsapp.utils.shareNews
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment : Fragment(R.layout.fragment_search_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: ArticleAdapter
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
                R.id.action_breakingNewsFragment_to_articleFragment,
                bundle
            )
        }
        newsAdapter.onSaveClickListener {
            viewModel.insertArticle(it)
            Snackbar.make(requireView(), "Saved", Snackbar.LENGTH_SHORT).show()
        }

        newsAdapter.onDeleteClickListener {
            viewModel.deleteArticle(it)
            Snackbar.make(requireView(), "Removed", Snackbar.LENGTH_SHORT).show()
        }

        newsAdapter.onShareNewsClickListener {
            shareNews(context, it)
        }
    }

    private fun setupRecycleView() {
        newsAdapter = ArticleAdapter()
        binding.rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }


        var searchJob: Job? = null
        binding.editTextSearch.addTextChangedListener { edit ->
            searchJob?.cancel()
            searchJob = MainScope().launch {
                delay(SEARCH_TIME_DELAY)
                edit?.let {
                    if (!edit.toString().trim().isEmpty()) {
                        viewModel.getSearchedNews(edit.toString())
                    }
                }
            }
        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    binding.apply {
                        shimmerFrameLayout.stopShimmer()
                        shimmerFrameLayout.visibility = View.GONE
                    }
                    response.data?.let { news -> newsAdapter.diff.submitList(news.articles) }
                }

                is Resource.Error -> {
                    binding.apply {
                        shimmerFrameLayout.stopShimmer()
                        shimmerFrameLayout.visibility = View.GONE
                        response.message?.let { message ->
                            Log.e(TAG, "Error: $message")
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
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "SearchNewsFragment"
        private const val SEARCH_TIME_DELAY = 500L
    }
}