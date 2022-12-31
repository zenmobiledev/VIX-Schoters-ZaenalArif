package com.schoters.newsapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.schoters.newsapp.R
import com.schoters.newsapp.databinding.ItemArticleBinding
import com.schoters.newsapp.model.Article

class ArticleAdapter : RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {

    class ArticleViewHolder(var view: ItemArticleBinding) : RecyclerView.ViewHolder(view.root)

    val diff = AsyncListDiffer(this, diffUtil)

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

        holder.view.imageViewSaved.setOnClickListener {
            if (holder.view.imageViewSaved.tag.toString().toInt() == 0) {
                holder.view.imageViewSaved.tag = 1
                holder.view.imageViewSaved.setImageDrawable(it.resources.getDrawable(R.drawable.ic_saved))
                onArticleSaveClick?.let {
                    if (article != null) {
                        it(article)
                    }
                }
            } else {
                holder.view.imageViewSaved.tag = 0
                holder.view.imageViewSaved.setImageDrawable(it.resources.getDrawable(R.drawable.ic_bookmark))
                onArticleSaveClick?.let {
                    if (article != null) {
                        it(article)
                    }
                }
            }
            onArticleSaveClick?.let {
                article?.let { it1 -> it(it1) }
            }
        }
    }

    override fun getItemCount(): Int = diff.currentList.size

    var isSave = false

    override fun getItemId(position: Int): Long = position.toLong()

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

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Article>() {
            override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean =
                oldItem.url == newItem.url

            override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean =
                newItem.title == oldItem.title
        }
    }
}