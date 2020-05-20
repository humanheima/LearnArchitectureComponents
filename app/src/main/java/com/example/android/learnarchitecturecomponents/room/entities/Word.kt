package com.example.android.learnarchitecturecomponents.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by dumingwei on 2020-03-11.
 * Desc:
 */
@Entity(tableName = "word_table")
data class Word(
        @PrimaryKey @ColumnInfo(name = "word") val word: String,
        val name: String? = null
)