package com.schoters.newsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
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

            binding.imageViewSaved.setOnClickListener { it ->
                if (binding.imageViewSaved.tag.toString().toInt() == 0) {
                    binding.imageViewSaved.tag = 1
//                    binding.imageViewSaved.setImageDrawable(it.resources.getDrawable(R.drawable.ic_saved))
                    binding.imageViewSaved.setImageDrawable(
                        ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.ic_saved
                        )
                    )
                    onArticleSaveClick?.let { click ->
                        click(article)
                    }
                } else {
                    binding.imageViewSaved.tag = 0
                    binding.imageViewSaved.setImageDrawable(
                        ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.ic_bookmark
                        )
                    )
                    onArticleSaveClick?.let { click ->
                        click(article)
                    }
                }
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