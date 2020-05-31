package com.example.android.jetpackdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Crete by dumingwei on 2020-03-11
 * Desc:
 *
 */
class ExampleActivity : AppCompatActivity() {


    companion object {

        val CLASS_NAME = "CLASS_NAME"

        fun launch(context: Context, clazz: Class<*>) {
            val intent = Intent(context, ExampleActivity::class.java)
            intent.putExtra(CLASS_NAME, clazz)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        val clazz = intent.getSerializableExtra(CLASS_NAME) as Class<*>
        val fragment = clazz.newInstance() as Fragment

        //val roomFragment = BasicRoomFragment.newInstance()
        supportFragmentManager.beginTransaction()
                .add(R.id.fl_container, fragment)
                .commit()
    }
}
