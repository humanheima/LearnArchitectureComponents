package com.example.architecturecomponents.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CounterViewModel : ViewModel() {

    private val counterModel = CounterModel() // 持有 Model
    private val _count = MutableLiveData<Int>(counterModel.count) // 初始值为 Model 的 count

    // 暴露不可变的 LiveData 给 View
    val count: LiveData<Int> get() = _count

    // 处理增加计数的逻辑
    fun incrementCount() {
        counterModel.increment() // 更新 Model
        _count.value = counterModel.count // 更新 LiveData
    }

}