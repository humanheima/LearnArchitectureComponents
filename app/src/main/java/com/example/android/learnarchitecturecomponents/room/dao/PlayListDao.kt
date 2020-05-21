package com.example.android.learnarchitecturecomponents.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.android.learnarchitecturecomponents.room.entities.NameAndDescription
import com.example.android.learnarchitecturecomponents.room.entities.Playlist
import com.example.android.learnarchitecturecomponents.room.entities.Word

/**
 * Created by dumingwei on 2020/5/21.
 *
 * Desc:
 */
@Dao
interface PlaylistDao {

    @Insert(entity = Playlist::class)
    suspend fun insert(playlist: Playlist)

    @Insert(entity = Playlist::class)
    suspend fun insertPartial(nameDescription: NameAndDescription)

    @Update(entity = Playlist::class)
    suspend fun updatePartial(nameDescription: NameAndDescription)

    @Delete(entity = Playlist::class)
    suspend fun deletePartial(nameDescription: NameAndDescription)

    @Query("SELECT * FROM Playlist")
    fun getList(): LiveData<List<Playlist>>

}