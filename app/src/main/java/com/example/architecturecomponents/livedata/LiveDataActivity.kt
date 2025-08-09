package com.example.architecturecomponents.livedata

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProvider
import com.example.architecturecomponents.R
import com.example.architecturecomponents.databinding.ActivityLiveDataBinding

/**
 * Crete by dumingwei on 2020-03-11
 * Desc: 学习LiveData
 */
class LiveDataActivity : AppCompatActivity() {

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, LiveDataActivity::class.java)
            context.startActivity(intent)
        }
    }

    private val TAG: String = javaClass.simpleName
    private lateinit var model: NameViewModel
    private lateinit var foreverObserver: Observer<String>

    // 使用 ViewModelProvider 获取 ViewModel 实例
    private lateinit var counterViewModel: CounterViewModel

    /**
     * onRestoreInstanceState 方法 在onCreate 之后调用
     *
     * @param savedInstanceState the data most recently supplied in [.onSaveInstanceState].
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.e(TAG, "onRestoreInstanceState: ")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.e(TAG, "onCreate: this$this")
        super.onCreate(savedInstanceState)
        val binding = ActivityLiveDataBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)


        model = ViewModelProvider(this).get(NameViewModel::class.java)
        Log.i(TAG, "onCreate: model = $model")

        model.currentName.setValue("Hello world")

        //binding.setModel(model);
        //binding.setLifecycleOwner(this);

        //观察者要和LifecycleOwner
        model.currentName.observe(this) { newName ->

            Log.i(TAG, "开始LiveData 值为null，是不会收到回调的 onChanged:: newName = $newName")
//            Log.i(
//                TAG,
//                "开始LiveData 值为null，是不会收到回调的 onChanged: $newName\n ${
//                    Log.getStackTraceString(Throwable())
//                }"
//            )
        }

        model.sport.observe(this, object : Observer<String> {
            override fun onChanged(t: String?) {
                Log.e(TAG, "开始LiveData，就不为null onChanged: $t")
            }
        })

        foreverObserver = Observer { newName -> // Update the UI, in this case, a TextView.
            Log.e(TAG, "foreverObserver onChanged: $newName")
        }

        /**
         * 永久观察，不需要关联LifecycleOwner，注意要在适当的时候调用removeObserver(Observer)来移除这些观察者
         */
        //model.currentName.observeForever(foreverObserver)

        counterViewModel = ViewModelProvider(this).get(CounterViewModel::class.java)


        // 观察 ViewModel 中的 LiveData，更新 UI
        counterViewModel.count.observe(this, Observer { count ->
            binding.tvCount.text = "count = $count"
        })

        // 按钮点击事件，触发 ViewModel 的逻辑
        binding.btnIncreaseCount.setOnClickListener {
            counterViewModel.incrementCount()
        }
    }

    private val anotherObserver: Observer<String> =
        Observer { newName -> // Update the UI, in this case, a TextView.
            Log.e(TAG, "onChanged: $newName")
        }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_observe_another -> {
                model.currentName.observe(this, anotherObserver)
            }

            R.id.btnManualRecreateActivity -> recreate()
            R.id.btnUpdateLiveData -> {
                val data = Transformations.map(
                    model.currentName
                ) { input -> "map result $input" }
                val data1 = Transformations.switchMap<String, String>(
                    model.currentName
                ) { null }
                model.currentName.setValue("name发生了改变" + System.currentTimeMillis())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        model.currentName.removeObserver(foreverObserver!!)
    }
}
