package com.example.architecturecomponents

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.architecturecomponents.activityresult.GetActivityResultActivity
import com.example.architecturecomponents.lifecycle.LifeCycleActivity
import com.example.architecturecomponents.livedata.LiveDataActivity
import com.example.architecturecomponents.room.BasicRoomFragment
import com.example.architecturecomponents.room.CodeLabRoomExampleFragment

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
    }

    fun onClick(v: View) {
        when (v.id) {
            R.id.btnTestGetActivityResult -> {
                GetActivityResultActivity.launch(this)
            }

            R.id.btnLiveData -> LiveDataActivity.launch(this)

            R.id.btnBasicRoom -> {
                ExampleActivity.launch(
                    this,
                    BasicRoomFragment::class.java
                )
            }

            R.id.btnRoomCodelabs -> {
                ExampleActivity.launch(
                    this,
                    CodeLabRoomExampleFragment::class.java
                )
            }

            R.id.btnLifecycle -> {
                LifeCycleActivity.launch(this)
            }

        }
    }

}
