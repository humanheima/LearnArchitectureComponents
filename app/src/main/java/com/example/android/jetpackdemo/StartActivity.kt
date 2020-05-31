package com.example.android.jetpackdemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.android.jetpackdemo.livedata.LiveDataActivity
import com.example.android.jetpackdemo.room.BasicRoomFragment
import com.example.android.jetpackdemo.room.CodeLabRoomExampleFragment

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btnLiveData -> LiveDataActivity.launch(this)
            R.id.btnBasicRoom -> {
                ExampleActivity.launch(this, BasicRoomFragment::class.java)
            }
            R.id.btnRoomCodelabs -> {
                ExampleActivity.launch(this, CodeLabRoomExampleFragment::class.java)
            }

        }
    }

}
