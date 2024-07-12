package com.schoters.newsapp.ui.fragment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.schoters.newsapp.R
import com.schoters.newsapp.databinding.ItemArticleBinding
import com.schoters.newsapp.model.Article

class ArticleAdapter : ListAdapter<Article, ArticleAdapter.ArticleViewHolder>(DIFF_CALLBACK) {

    private var onItemClickListener: ((Article) -> Unit)? = null
    private var onArticleSaveClick: ((Article) -> Unit)? = null
    private var onArticleDeleteClick: ((Article) -> Unit)? = null
    private var onShareNewsClick: ((Article) -> Unit)? = null

    fun setOnItemClickListener(listener: ((Article) -> Unit)) {
        onItemClickListener = listener
    }

    fun onSaveClickListener(listener: ((Article) -> Unit)) {
        onArticleSaveClick = listener
    }

    fun onDeleteClickListener(listener: ((Article) -> Unit)) {
        onArticleDeleteClick = listener
    }

    fun onShareNewsClickListener(listener: ((Article) -> Unit)) {
        onShareNewsClick = listener
    }

    inner class ArticleViewHolder(private val binding: ItemArticleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.article = article
            binding.executePendingBindings()

            itemView.setOnClickListener {
                onItemClickListener?.let { click ->
                    click(article)
                }
            }

            binding.imageViewShare.setOnClickListener {
                onShareNewsClick?.let { click ->
                    click(article)
                }
            }

            with(binding) {
                constraintLayout.setTransitionListener(object :
                    MotionLayout.TransitionListener {
                    override fun onTransitionStarted(
                        motionLayout: MotionLayout?,
                        startId: Int,
                        endId: Int
                    ) {

                    }

                    override fun onTransitionChange(
                        motionLayout: MotionLayout?,
                        startId: Int,
                        endId: Int,
                        progress: Float
                    ) {

                    }

                    override fun onTransitionCompleted(
                        motionLayout: MotionLayout?,
                        currentId: Int
                    ) {
                        if (currentId == R.id.end) {
                            motionLayout?.post {
                                motionLayout.transitionToState(R.id.start)
                                motionLayout.progress = 0F
                                onArticleSaveClick?.let { click ->
                                    click(article)
                                }
                            }
                        }
                    }

                    override fun onTransitionTrigger(
                        motionLayout: MotionLayout?,
                        triggerId: Int,
                        positive: Boolean,
                        progress: Float
                    ) {
                        if (positive && triggerId == R.id.image_view_saved) {
                            imageViewSaved.setOnClickListener {
                                Toast.makeText(
                                    itemView.context,
                                    "SAVED",
                                    Toast.LENGTH_SHORT
                                ).show()
                                motionLayout?.transitionToEnd()
                            }
                        }

                    }

                })

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<ItemArticleBinding>(
            inflater,
            R.layout.item_article,
            parent,
            false
        )
        return ArticleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = getItem(position)
        holder.bind(article)
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
                oldItem.url == newItem.url

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
                oldItem == newItem
        }
    }
}