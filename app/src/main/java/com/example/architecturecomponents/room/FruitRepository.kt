package com.example.architecturecomponents.room

import androidx.lifecycle.LiveData
import com.example.architecturecomponents.room.dao.FruitDao
import com.example.architecturecomponents.room.entities.Fruit

/**
 * Created by dumingwei on 2020-03-12.
 * Desc:
 */
class FruitRepository(private val fruitDao: FruitDao) {

    val fruits = fruitDao.fruits

    suspend fun insert(fruit: com.example.architecturecomponents.room.entities.Fruit) {
        return fruitDao.insertFruit(fruit)
    }

    fun query(id: Long): LiveData<com.example.architecturecomponents.room.entities.Fruit?> {
        return fruitDao.queryFruitWithId(id)
    }
}