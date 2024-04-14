package com.example.architecturecomponents.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by dumingwei on 2020/5/21.
 *
 * Desc:
 */
@Entity
data class Playlist(
        @PrimaryKey(autoGenerate = true)
        val playlistId: Long,
        val name: String,
        val description: String,
        @ColumnInfo(defaultValue = "normal")
        val category: String,
        @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
        val createdTime: String,
        @ColumnInfo(defaultValue = "CURRENT_TIMESTAMP")
        val lastModifiedTime: String
)