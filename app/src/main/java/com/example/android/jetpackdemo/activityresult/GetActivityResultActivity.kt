package com.example.android.jetpackdemo.activityresult

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.android.jetpackdemo.R

/**
 * Created by p_dmweidu on 2023/9/18
 * Desc: 测试新的获取ActivityResult的方式
 */
class GetActivityResultActivity : AppCompatActivity() {

    private val TAG = "GetActivityResultActivi"

    private var getContentFirst: ActivityResultLauncher<Intent>? = null
    private var getContentSecond: ActivityResultLauncher<Intent>? = null

    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null

    companion object {

        fun launch(context: Context) {
            val starter = Intent(context, GetActivityResultActivity::class.java)
            context.startActivity(starter)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_result)

        //使用 getContentFirst 启动，就会走到这里的回调
        getContentFirst = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                Log.i(TAG, "onCreate: data = ${data?.getStringExtra("data")} ")
                // Handle the Intent
            }
        }

        //使用 getContentSecond 启动，就会走到这里的回调
        getContentSecond = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                Log.i(TAG, "onCreate: 第二个启动 data = ${data?.getStringExtra("data")} ")
                // Handle the Intent
            }
        }

        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
                for (entry in result) {
                    Log.i(TAG, "onCreate: 权限 ${entry.key} , granted = ${entry.value}")
                }

            }
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_first -> {
                val intent = Intent(this, TargetActivity::class.java)
                getContentFirst?.launch(intent)
            }

            R.id.btn_second -> {
                val intent = Intent(this, TargetActivity::class.java)
                getContentSecond?.launch(intent)
            }
            R.id.btn_request_permission -> {
                val permissions = arrayOf<String>(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                requestPermissionLauncher?.launch(permissions)
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray//PERMISSION_GRANTED or PERMISSION_DENIED
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //即使使用 requestPermissionLauncher 的方式，这里也会收到回调。
        Log.i(TAG, "onRequestPermissionsResult: ")
    }

}