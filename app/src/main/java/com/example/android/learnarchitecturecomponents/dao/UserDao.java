package com.example.android.learnarchitecturecomponents.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.android.learnarchitecturecomponents.entities.NameTuple;
import com.example.android.learnarchitecturecomponents.entities.User;

import java.util.List;

/**
 * Created by DuMingwei on 2018/8/1.
 * Description:
 */
@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertUser(User users);

    @Insert
    void insertBoth(User user1, User user2);

    @Insert
    void insertWithFriends(User user, List<User> friends);

    @Query("SELECT * FROM user")
    List<User> loadAllUser();

    @Query("SELECT * FROM user WHERE id<:id")
    List<User> loadUserWithId(long id);

    /**
     * 只查询某些列,查出来的数据条数不对
     *
     * @return
     */
    @Query("SELECT first_name,lastName FROM user")
    List<NameTuple> loadFullName();

    @Update
    int updateUsers(User... users);

    @Delete
    int deleteUsers(User... users);
}
