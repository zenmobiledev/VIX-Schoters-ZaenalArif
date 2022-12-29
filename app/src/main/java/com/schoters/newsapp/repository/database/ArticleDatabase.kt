package com.schoters.newsapp.repository.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.schoters.newsapp.model.Article

@Database(entities = [Article::class], version = 3)

@TypeConverters(Converters::class)
abstract class ArticleDatabase : RoomDatabase() {
    abstract fun getArticleDao(): ArticleDAO

    companion object {
        @Volatile
        private var articleDatabaseInstance: ArticleDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = articleDatabaseInstance ?: synchronized(LOCK) {
            articleDatabaseInstance ?: createDatabaseInstance(context).also {
                articleDatabaseInstance = it
            }
        }

        private fun createDatabaseInstance(context: Context) = Room.databaseBuilder(
            context, ArticleDatabase::class.java,
            "articles_db.db"
        ).fallbackToDestructiveMigration().build()
    }
}