package com.example.android.jetpackdemo.lifecycle

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

/**
 * Created by dumingwei on 2020/5/27.
 *
 * Desc:
 */
class MyObserver(val name: String? = "") : LifecycleObserver {

    private val TAG: String? = "MyObserver"


    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(owner: LifecycleOwner) {
        Log.d(TAG, "${name},onCreate: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "${name},onStart: ")

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(owner: LifecycleOwner) {
        Log.d(TAG, "${name},onResume: ")

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause(owner: LifecycleOwner) {
        Log.d(TAG, "${name},onPause: ")

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "${name},onStop: ")

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(owner: LifecycleOwner) {
        Log.d(TAG, "${name},onDestroy: ")
    }


}