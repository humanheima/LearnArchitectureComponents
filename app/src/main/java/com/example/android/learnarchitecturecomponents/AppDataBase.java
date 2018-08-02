package com.example.android.learnarchitecturecomponents;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.learnarchitecturecomponents.dao.UserDao;
import com.example.android.learnarchitecturecomponents.entities.User;

/**
 * Created by DuMingwei on 2018/8/2.
 * Description:
 */
@Database(entities = {User.class}, version = 1)
public abstract class AppDataBase extends RoomDatabase {

    private static final String TAG = AppDataBase.class.getSimpleName();

    private static final String DATABASE_NAME = "basic-sample-db";
    private static AppDataBase instance;

    public abstract UserDao userDao();

    public static AppDataBase getInstance(Context context, final AppExecutors executors) {
        if (instance == null) {
            synchronized (AppDataBase.class) {
                if (instance == null) {
                    instance = buildDataBase(context.getApplicationContext(), executors);
                }
            }
        }
        return instance;
    }

    private static AppDataBase buildDataBase(Context applicationContext, AppExecutors executors) {
        return Room.databaseBuilder(applicationContext, AppDataBase.class, DATABASE_NAME)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Log.d(TAG, "buildDataBase onCreate: ");
                    }
                }).build();
    }
}
