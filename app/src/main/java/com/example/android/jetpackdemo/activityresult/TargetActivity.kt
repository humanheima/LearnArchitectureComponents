package com.example.android.jetpackdemo.activityresult

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.android.jetpackdemo.R

/**
 * Created by p_dmweidu on 2023/9/18
 * Desc: 启动的目标Activity
 */
class TargetActivity : AppCompatActivity() {

    private var btnFinish: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_target)
        btnFinish = findViewById(R.id.btn_finish)
        btnFinish?.setOnClickListener {
            setResult(RESULT_OK, Intent().apply {
                putExtra("data", "这是第一个返回的数据")
            })
            finish()
        }
    }


}