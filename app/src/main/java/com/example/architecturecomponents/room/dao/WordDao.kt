package com.example.architecturecomponents.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.architecturecomponents.room.entities.Word

/**
 * Created by dumingwei on 2020-03-11.
 * Desc:
 */
@Dao
interface WordDao {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(word: Word)

    //插入两条数据
    @Insert
    suspend fun insert(word1: Word, word2: Word)

    //可变参数
    @Insert
    fun insert(vararg words: Word)

    //插入列表
    @Insert
    suspend fun insert(wordList: List<Word>)

    /**
     * 因为没有一个注解可以用来删除多个条目，所以用的是@Query注解，注意一下
     */
    @Query("DELETE FROM word_table")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(word: Word): Int


    @Delete
    suspend fun delete(word1: Word, word2: Word): Int

    //可变参数
    @Insert
    fun delete(vararg words: Word)

    //插入列表
    @Delete
    suspend fun delete(wordList: List<Word>)

    @Update
    suspend fun update(word: Word): Int

    @Update
    suspend fun update(word1: Word, word2: Word): Int

    @Update
    suspend fun update(vararg words: Word): Int

    @Update
    suspend fun update(wordList: List<Word>): Int

    /**
     * 真正的查询默认是在工作线程的，Room内部为我们做了一些线程切换的工作，暂时不去管
     */
    @Query("SELECT * FROM word_table ORDER BY word ASC")
    fun getAlphabetizedWords(): LiveData<List<Word>>
}