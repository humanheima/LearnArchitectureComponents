package com.example.android.learnarchitecturecomponents.room

import androidx.lifecycle.LiveData
import com.example.android.learnarchitecturecomponents.room.dao.FruitDao
import com.example.android.learnarchitecturecomponents.room.entities.Fruit

/**
 * Created by dumingwei on 2020-03-12.
 * Desc:
 */
class FruitRepository(private val fruitDao: FruitDao) {

    val fruits = fruitDao.fruits

    suspend fun insert(fruit: Fruit) {
        return fruitDao.insertFruit(fruit)
    }

    fun query(id: Long): LiveData<Fruit?> {
        return fruitDao.queryFruitWithId(id)
    }
}