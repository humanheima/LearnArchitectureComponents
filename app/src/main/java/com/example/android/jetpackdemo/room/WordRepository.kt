package com.example.android.jetpackdemo.room

import androidx.lifecycle.LiveData
import com.example.android.jetpackdemo.room.dao.WordDao
import com.example.android.jetpackdemo.room.entities.Word

/**
 * Created by dumingwei on 2020-03-11.
 * Desc:
 */
//构造函数中传入的是WordDao而不是整个数据库对象
class WordRepository(private val wordDao: WordDao) {

    // Room 会在后台线程执行查询操作，我们可以在主线程调用查询方法。
    // 当数据发生变化当时候，LiveData会在主线程通知所有的观察者
    val allWords: LiveData<List<Word>> = wordDao.getAlphabetizedWords()

    //阻塞方法，需要在协程中或另外一个阻塞方法中调用
    suspend fun insert(word: Word) {
        wordDao.insert(word)
    }
}