package com.example.architecturecomponents.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.example.architecturecomponents.room.entities.Fruit

@Dao
interface FruitDao {

    @get:Query("SELECT * FROM fruit")
    val fruits: LiveData<List<com.example.architecturecomponents.room.entities.Fruit>>

    @Query("SELECT * FROM fruit WHERE id= :id LIMIT 1")
    fun queryFruitWithId(id: Long): LiveData<com.example.architecturecomponents.room.entities.Fruit?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFruit(fruit: com.example.architecturecomponents.room.entities.Fruit)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertFruits(fruitList: List<com.example.architecturecomponents.room.entities.Fruit>)
}
