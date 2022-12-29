package com.schoters.newsapp.utils

import android.content.Context
import android.content.Intent
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.schoters.newsapp.R
import com.schoters.newsapp.model.Article

fun shareNews(context: Context?, article: Article) {
    val intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, article.urlToImage)
        putExtra(Intent.EXTRA_STREAM, article.urlToImage)
        putExtra(Intent.EXTRA_TITLE, article.title)
        type = "image/*"
    }
    context?.startActivity(Intent.createChooser(intent, "Share News on"))
}

fun getCircularDraw(context: Context): CircularProgressDrawable =
    CircularProgressDrawable(context).apply {
        strokeWidth = 8f
        centerRadius = 48f
        setTint(ContextCompat.getColor(context, R.color.teal_700))
        start()
    }

fun ImageView.loadImage(url: String, progressDraw: CircularProgressDrawable) {
    val options = RequestOptions()
        .placeholder(progressDraw)
        .error(R.drawable.ic_launcher_background)
    Glide.with(context)
        .setDefaultRequestOptions(options)
        .load(url)
        .into(this)
}

@BindingAdapter("loadImage")
fun loadImage(imageView: ImageView, url: String?) {
    if (url != null) {
        imageView.loadImage(url!!, getCircularDraw(imageView.context))
    }
}