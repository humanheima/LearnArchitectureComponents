package com.example.android.jetpackdemo;

import android.app.Application;

import com.example.android.jetpackdemo.room.AppDataBase;
import com.example.android.jetpackdemo.room.AppExecutors;
import com.facebook.stetho.Stetho;

import kotlinx.coroutines.GlobalScope;

/**
 * Created by DuMingwei on 2018/8/1.
 * Description:
 */
public class App extends Application {

    private static AppExecutors executors;
    private static App context;

    //private static WordRoomDatabase wordRoomDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        executors = new AppExecutors();
        context = this;
        //wordRoomDatabase = WordRoomDatabase.getDatabase(this);
    }

    public static AppExecutors getExecutors() {
        return executors;
    }

    public static AppDataBase getDataBase() {
        return AppDataBase.Companion.getInstance(context, GlobalScope.INSTANCE);
    }

   // public static WordRoomDatabase getWordRoomDatabase() {
        //return wordRoomDatabase;
    //}
}
