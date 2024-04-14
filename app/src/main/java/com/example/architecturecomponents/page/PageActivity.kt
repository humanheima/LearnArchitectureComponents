package com.example.architecturecomponents.page

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.architecturecomponents.R

/**
 * Created by dumingwei on 2020/5/25
 *
 * Desc: Page分页加载
 */
class PageActivity : AppCompatActivity() {


    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, PageActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_page)
    }
}
