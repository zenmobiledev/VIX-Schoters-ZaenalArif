package com.schoters.newsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.schoters.newsapp.R
import com.schoters.newsapp.databinding.ItemSavedNewsBinding
import com.schoters.newsapp.model.Article

class BookmarkAdapter : RecyclerView.Adapter<BookmarkAdapter.BookmarkViewHolder>() {

    private val diffUtil = object : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
            oldItem.url == oldItem.url

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
            oldItem.id == newItem.id

    }

    inner class BookmarkViewHolder(var view: ItemSavedNewsBinding) :
        RecyclerView.ViewHolder(view.root)

    val diff = AsyncListDiffer(this, diffUtil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = DataBindingUtil.inflate<ItemSavedNewsBinding>(
            inflater,
            R.layout.item_saved_news,
            parent,
            false
        )
        return BookmarkViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) {
        val article = diff.currentList[position]
        holder.view.article = article

        holder.itemView.setOnClickListener {
            onItemClickListener?.let {
                article.let { article -> it(article) }
            }
        }

        holder.view.imageViewShare.setOnClickListener {
            onShareNewsClick?.let {
                article.let { it1 -> it(it1) }
            }
        }
    }

    override fun getItemCount(): Int = diff.currentList.size

    private var onItemClickListener: ((Article) -> Unit)? = null
    private var onShareNewsClick: ((Article) -> Unit)? = null

    fun setOnItemClickListener(listener: ((Article) -> Unit)) {
        onItemClickListener = listener
    }

    fun onShareNewsClickListener(listener: ((Article) -> Unit)) {
        onShareNewsClick = listener
    }
}