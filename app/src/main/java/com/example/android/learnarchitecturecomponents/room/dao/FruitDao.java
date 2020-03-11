package com.example.android.learnarchitecturecomponents.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.android.learnarchitecturecomponents.room.entities.Fruit;

import java.util.List;

@Dao
public interface FruitDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertFruit(Fruit fruit);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insertFruits(List<Fruit> fruitList);

    @Query("SELECT * FROM fruit")
    LiveData<List<Fruit>> getFruits();
}
