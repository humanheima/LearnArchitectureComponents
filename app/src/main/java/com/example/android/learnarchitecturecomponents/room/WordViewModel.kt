package com.example.android.learnarchitecturecomponents.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.android.learnarchitecturecomponents.room.entities.Word
import kotlinx.coroutines.launch

/**
 * Created by dumingwei on 2020-03-11.
 * Desc:
 */

open class WordViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WordRepository

    val allWords: LiveData<List<Word>>

    init {

        val wordDAO = AppDataBase.getInstance(application, viewModelScope).wordDAO()
        repository = WordRepository(wordDAO)
        allWords = repository.allWords
    }

    fun insert(word: Word) = viewModelScope.launch {
        repository.insert(word)
    }

}