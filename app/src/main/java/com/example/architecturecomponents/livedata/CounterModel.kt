package com.example.architecturecomponents.livedata

/**
 * Created by p_dmweidu on 2025/8/9
 * Desc: 如果要做网络请求，还是要在ViewModel中做
 */
// Model
data class CounterModel(var count: Int = 0) {

    fun increment() {
        count++
    }
}