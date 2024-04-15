package com.example.architecturecomponents.lifecycle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.architecturecomponents.R

/**
 * Created by dumingwei on 2020/5/25
 *
 * Desc: Lifecycle
 */
class LifeCycleActivity : AppCompatActivity() {

    private val TAG: String? = "LifeCycleActivity"

    private val btnGetCurrentStatus: Button by lazy {
        findViewById(R.id.btnGetCurrentStatus)
    }

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, LifeCycleActivity::class.java)
            context.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_life_cycle)
        Log.d(TAG, "onCreate: lifecycle.currentState = ${lifecycle.currentState}")

        //lifecycle.addObserver(MyObserver())

//        btnAdd.setOnClickListener {
//            lifecycle.addObserver(MyObserver("Hello"))
//        }
//
//        btnGetCurrentStatus.setOnClickListener {
//            Log.d(TAG, "onCreate: lifecycle.currentState = ${lifecycle.currentState}")
//        }

    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: lifecycle.currentState = ${lifecycle.currentState}")

    }

    override fun onResume() {
        super.onResume()
        //输出 onResume: lifecycle.currentState = STARTED
        //为什么不是 RESUMED呢？
        Log.d(TAG, "onResume: lifecycle.currentState = ${lifecycle.currentState}")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: lifecycle.currentState = ${lifecycle.currentState}")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: lifecycle.currentState = ${lifecycle.currentState}")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: lifecycle.currentState = ${lifecycle.currentState}")
    }

}
