package com.schoters.newsapp.ui.fragment.features.saved

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.schoters.newsapp.MainActivity
import com.schoters.newsapp.R
import com.schoters.newsapp.ui.fragment.adapter.BookmarkAdapter
import com.schoters.newsapp.databinding.FragmentBookmarkNewsBinding
import com.schoters.newsapp.repository.viewmodel.NewsViewModel
import com.schoters.newsapp.utils.shareNews

class BookmarkNewsFragment : Fragment(R.layout.fragment_bookmark_news) {
    lateinit var viewModel: NewsViewModel
    lateinit var newsAdapter: BookmarkAdapter
    private var _binding: FragmentBookmarkNewsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentBookmarkNewsBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = (activity as MainActivity).viewModel
        setupRecycleView()
        setupViewModelObserver()

        val onItemSwipeHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = true

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val article = newsAdapter.diff.currentList[position]
                viewModel.deleteArticle(article)

                Snackbar.make(requireView(), "Delete Successfully", Snackbar.LENGTH_SHORT).apply {
                    setAction("Undo") {
                        viewModel.insertArticle(article)
                    }
                    show()
                }
            }
        }
        ItemTouchHelper(onItemSwipeHelperCallback).apply {
            attachToRecyclerView(binding.rvBookmarkNews)
        }


    }

    private fun setupViewModelObserver() {
        viewModel = (activity as MainActivity).viewModel

        viewModel.getBookmarkArticles().observe(viewLifecycleOwner) {
            newsAdapter.diff.submitList(it)
            binding.apply {
                rvBookmarkNews.visibility = View.VISIBLE
                shimmerFrameLayout.stopShimmer()
                shimmerFrameLayout.visibility = View.GONE
            }
        }
    }

    private fun setupRecycleView() {
        newsAdapter = BookmarkAdapter()

        binding.rvBookmarkNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
        }

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(
                R.id.action_savedNewsFragment_to_articleFragment,
                bundle
            )
        }

        newsAdapter.onShareNewsClickListener {
            viewModel.insertArticle(it)
            shareNews(context, it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}