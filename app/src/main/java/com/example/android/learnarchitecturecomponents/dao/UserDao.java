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

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;

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
     * 1. 数据库中没有记录的时候,Maybe会complete
     * 2. 数据库中有记录的时候会触发onSuccess，complete
     * 3. 如果complete被调用以后，数据被更新了，什么也不会发生
     *
     * @param id
     * @return
     */
    @Query("SELECT * FROM user WHERE id=:id")
    Maybe<User> findUserMaybeWithId(long id);

    /**
     * 1. 数据库中没有记录的时候会触发onError(EmptyResultSetException.class)
     * 2. 数据库中有记录的时候会触发onSuccess
     * 3. 如果Single.onComplete被调用以后，数据被更新了，什么也不会发生，因为这个数据流已经结束
     *
     * @param id
     * @return
     */
    @Query("SELECT * FROM user WHERE id=:id")
    Single<User> findUserWithId(long id);

    /**
     * 1. 当表中没有记录的时候，Flowable 什么都不发射
     * 2.当表中有一条记录的时候，触发onNext方法
     * 3. 当此条记录被更新的时候，Flowable会自动发射触发onNext方法
     *
     * @param userId
     * @return
     */
    @Query("SELECT * FROM user WHERE id = :userId")
    Flowable<User> getUserById(long userId);

    /**
     * 只查询某些列,查出来的数据条数不对,总是会少
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
