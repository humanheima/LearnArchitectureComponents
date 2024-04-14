package com.example.architecturecomponents.livedata

import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel

/**
 * Created by dmw on 2018/9/15.
 * Desc:
 */
class NameViewModel : ViewModel() {

    //创建一个持有String类型数据的LiveData
    val currentName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

}
