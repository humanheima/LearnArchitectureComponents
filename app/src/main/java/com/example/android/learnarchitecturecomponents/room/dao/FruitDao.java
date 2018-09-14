package com.example.android.learnarchitecturecomponents.room.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

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
