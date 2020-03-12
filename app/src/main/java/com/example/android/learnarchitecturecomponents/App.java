package com.example.android.learnarchitecturecomponents;

import android.app.Application;

import com.example.android.learnarchitecturecomponents.room.AppDataBase;
import com.example.android.learnarchitecturecomponents.room.AppExecutors;
import com.facebook.stetho.Stetho;

import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.GlobalScope;

/**
 * Created by DuMingwei on 2018/8/1.
 * Description:
 */
public class App extends Application {

    private static AppExecutors executors;
    private static App context;

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        executors = new AppExecutors();
        context = this;
    }

    public static AppExecutors getExecutors() {
        return executors;
    }

    public static AppDataBase getDataBase() {
        return AppDataBase.Companion.getInstance(context, GlobalScope.INSTANCE);
    }
}
