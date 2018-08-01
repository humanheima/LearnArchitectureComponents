package com.example.android.learnarchitecturecomponents.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.android.learnarchitecturecomponents.entities.User;

import java.util.List;

/**
 * Created by DuMingwei on 2018/8/1.
 * Description:
 */
@Dao
public interface UserDao {

    @Insert
    void insertUsers(User... users);

    @Insert
    void insertBoth(User user1, User user2);

    @Insert
    void insertWithFriends(User user, List<User> friends);

}
