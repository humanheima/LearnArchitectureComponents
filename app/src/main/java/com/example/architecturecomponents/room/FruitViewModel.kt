package com.example.architecturecomponents.room

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.architecturecomponents.room.entities.Fruit
import kotlinx.coroutines.launch

/**
 * Created by dumingwei on 2020-03-12.
 * Desc:
 */
class FruitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FruitRepository

    val fruits: LiveData<List<Fruit>>

    init {

        val fruitDao = AppDataBase.getInstance(application, viewModelScope).fruitDao()
        repository = FruitRepository(fruitDao)
        fruits = repository.fruits
    }

    fun insert(fruit: com.example.architecturecomponents.room.entities.Fruit) =
        viewModelScope.launch {
            repository.insert(fruit)
        }


    fun query(id: Long): LiveData<com.example.architecturecomponents.room.entities.Fruit?> {
        return repository.query(id)
    }

}