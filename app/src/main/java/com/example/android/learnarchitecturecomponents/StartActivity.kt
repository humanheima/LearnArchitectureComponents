package com.example.android.learnarchitecturecomponents

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.android.learnarchitecturecomponents.livedata.LiveDataActivity

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btnLiveData -> LiveDataActivity.launch(this)
        }
    }

}
