package com.example.android.learnarchitecturecomponents.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.android.learnarchitecturecomponents.room.dao.PlaylistDao
import com.example.android.learnarchitecturecomponents.room.dao.WordDao
import com.example.android.learnarchitecturecomponents.room.entities.Playlist
import com.example.android.learnarchitecturecomponents.room.entities.Word
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Created by dumingwei on 2020/5/19.
 *
 * Desc:
 */

@Database(entities = [Word::class, Playlist::class], version = 1, exportSchema = true)
abstract class WordRoomDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    abstract fun playlistDao(): PlaylistDao

    companion object {

        @JvmStatic
        @Volatile
        private var INSTANCE: WordRoomDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context, scope: CoroutineScope): WordRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, WordRoomDatabase::class.java,
                        "word_database")
                        .addCallback(WordDatabaseCallback(scope))
                        .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    class WordDatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {

        private val TAG = "WordDatabaseCallback"

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            Log.d(TAG, "onOpen: ")
            INSTANCE?.let { database ->
                scope.launch {
                    //populateDatabase(database.wordDao())
                }
            }

        }

        private suspend fun populateDatabase(wordDao: WordDao) {
            // Delete all content here.
            wordDao.deleteAll()
            var word = Word("Hello")
            wordDao.insert(word)
            word = Word("World!")
            wordDao.insert(word)

            word = Word("TODO!")
            wordDao.insert(word)

            word = Word("吃瓜!")
            wordDao.insert(word)
        }
    }


}