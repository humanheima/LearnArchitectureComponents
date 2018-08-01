package com.example.android.learnarchitecturecomponents;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by DuMingwei on 2018/8/1.
 * Description:
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
