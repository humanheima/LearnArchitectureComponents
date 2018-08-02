package com.example.android.learnarchitecturecomponents;

import android.app.Application;
import android.content.Context;

import com.facebook.stetho.Stetho;

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
        return AppDataBase.getInstance(context, executors);
    }
}
