package com.example.android.jetpackdemo

import android.Manifest
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.android.jetpackdemo.lifecycle.LifeCycleActivity
import com.example.android.jetpackdemo.livedata.LiveDataActivity
import com.example.android.jetpackdemo.room.BasicRoomFragment
import com.example.android.jetpackdemo.room.CodeLabRoomExampleFragment
import com.example.android.jetpackdemo.room.WordRoomDatabase
import com.example.android.jetpackdemo.room.dao.WordDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread


class StartActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val TAG: String? = "StartActivity"

    //共享资源
    val lockedResource: Any = Any()

    lateinit var wordDAO: WordDao

    val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        requiresTwoPermission()
        Log.d(TAG, "onCreate: ${dateFormat.format(System.currentTimeMillis())}")
        wordDAO = WordRoomDatabase.getDatabase(this, this).wordDao()
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
            R.id.btnLifecycle -> {
                LifeCycleActivity.launch(this)
            }
            R.id.btnSort -> {
                sortBigArray()
            }
            R.id.btnDoIo -> {
                doIo()
            }
            R.id.btnWaitLockedResource -> {
                LockTask().execute(arrayListOf<Int>())
                Log.d(TAG, "onClick: 主线程先睡眠一会，避免先获取到锁")
                Thread.sleep(200)
                Log.d(TAG, "onClick: 主线程先睡眠结束，尝试获取锁")
                synchronized(lockedResource) {
                    for (index in 0 until 10) {
                        Log.d(TAG, "onClick: 主线程获取到锁了$index")
                    }
                }
            }

            R.id.btnDeadLock -> {
                mockDeadLock()
            }
        }
    }

    /**
     * 对一个大数组排序
     */
    private fun sortBigArray() {
        Log.d(TAG, "sortBigArray: 开始初始化数组${dateFormat.format(System.currentTimeMillis())}")
        val currTime = System.currentTimeMillis()
        val random = IntArray(1000000)
        for (i in random.indices) {
            random[i] = (Math.random() * 10000000).toInt()
        }
        Log.d(TAG, "sortBigArray: 初始化数组完毕，开始排序${dateFormat.format(System.currentTimeMillis())}")
        BubbleSort.sort(random)
        Log.d(TAG, "sortBigArray: 数组排序完毕${dateFormat.format(System.currentTimeMillis())}")
        Log.d(TAG, "耗时：+ ${(System.currentTimeMillis() - currTime)}ms")
    }

    /**
     * 拷贝文件，主要要有读写权限
     */
    private fun doIo() {
        val prePath = Environment.getExternalStorageDirectory().path
        val file = File("${prePath}/test/View.java")
        if (file.exists()) {
            Log.d(TAG, "doIo: ${file.length()}")

            val reader = FileReader(file)
            val fileWriter = FileWriter("${prePath}/test/ViewCopy.java", true)

            for (index in 0 until 5) {
                var count: Int
                while (reader.read().also { count = it } != -1) {
                    fileWriter.write(count)
                }
                try {
                    reader.reset()
                } catch (e: IOException) {
                    Log.d(TAG, "doIo: error ${e.message}")
                }
            }
        }
    }

    inner class LockTask : AsyncTask<MutableList<Int>, Int, Unit>() {
        override fun doInBackground(vararg params: MutableList<Int>) =
                synchronized(lockedResource) {
                    val list = params[0]
                    for (i in 0 until 1000000) {
                        list.add((Math.random() * 10000000).toInt())
                    }
                    list.forEach {
                        Log.d(TAG, "doInBackground: for each element is $it")
                    }
                }
    }


    val resourceFirst = "resourceFirst"
    val resourceSecond = "resourceSecond"

    private fun mockDeadLock() {
        //工作线程获取了两把锁
        thread(start = false) {
            synchronized(resourceSecond) {
                Log.d(TAG, "工作线程获取了锁 resourceSecond")
                Thread.sleep(100)
                Log.d(TAG, "工作线程尝试获取锁 resourceFirst")
                synchronized(resourceFirst) {
                    while (true) {
                        Log.d(TAG, "主线程 mockDeadLock")
                    }
                }
            }
        }.start()

        //主线程睡眠30ms后开始获取锁
        Thread.sleep(30)

        synchronized(resourceFirst) {
            Log.d(TAG, "主线程获取了锁 resourceFirst")

            Log.d(TAG, "主线程尝试获取锁 resourceSecond")
            synchronized(resourceSecond) {
                Log.d(TAG, "主线程获取了锁 resourceFirst")
                while (true) {
                    Log.d(TAG, "主线程 mockDeadLock")
                }
            }
        }


    }

    @AfterPermissionGranted(REQUEST_CODE)
    private fun requiresTwoPermission() {
        val perms = arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (EasyPermissions.hasPermissions(this, *perms)) {
            Toast.makeText(this, "已获得读写权限", Toast.LENGTH_SHORT).show()
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "请求权限",
                    REQUEST_CODE, perms.component1(), perms.component2())
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }

    companion object {

        const val REQUEST_CODE = 100
    }

}

