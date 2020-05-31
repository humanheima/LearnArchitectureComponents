package com.example.android.jetpackdemo.lifecycle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.android.jetpackdemo.R
import kotlinx.android.synthetic.main.activity_life_cycle.*

/**
 * Created by dumingwei on 2020/5/25
 *
 * Desc: Lifecycle
 */
class LifeCycleActivity : AppCompatActivity() {

    private val TAG: String? = "LifeCycleActivity"


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

        btnAdd.setOnClickListener {
            lifecycle.addObserver(MyObserver("Hello"))
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: lifecycle.currentState = ${lifecycle.currentState}")

    }

    override fun onResume() {
        super.onResume()
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
