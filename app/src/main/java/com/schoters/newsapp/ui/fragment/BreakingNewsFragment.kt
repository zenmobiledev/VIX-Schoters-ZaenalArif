package com.schoters.newsapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.schoters.newsapp.MainActivity
import com.schoters.newsapp.R
import com.schoters.newsapp.adapter.ArticleAdapter
import com.schoters.newsapp.databinding.FragmentBreakingNewsBinding
import com.schoters.newsapp.repository.viewmodel.NewsViewModel
import com.schoters.newsapp.utils.Resource
import com.schoters.newsapp.utils.shareNews
import kotlin.random.Random

class BreakingNewsFragment : Fragment(R.layout.fragment_breaking_news) {

    private lateinit var viewModel: NewsViewModel
    private lateinit var newsAdapter: ArticleAdapter

    private var _binding: FragmentBreakingNewsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBreakingNewsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel
        setupRecycleView()
        setupViewModelObserver()
    }

    private fun setupRecycleView() {
        newsAdapter = ArticleAdapter()
        binding.rvBreakingNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }

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
            if (it.id == null) {
                it.id = Random.nextInt(0, 1000)
            }
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

    private fun setupViewModelObserver() {
        viewModel.breakingNews.observe(viewLifecycleOwner) { response ->
            when (response) {
                is Resource.Success -> {
                    binding.shimmerFrameLayout.stopShimmer()
                    binding.shimmerFrameLayout.visibility = View.GONE
                    response.data?.let { news ->
                        binding.rvBreakingNews.visibility = View.VISIBLE
                        newsAdapter.submitList(news.articles)
                    }
                }

                is Resource.Error -> {
                    binding.shimmerFrameLayout.visibility = View.GONE
                    response.message?.let { message ->
                        Toast.makeText(activity, "An error occurred: $message", Toast.LENGTH_SHORT)
                            .show()
                    }
                }

                is Resource.Loading -> {
                    binding.shimmerFrameLayout.startShimmer()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}