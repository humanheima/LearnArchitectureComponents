package com.example.android.learnarchitecturecomponents.livedata;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

/**
 * Created by dmw on 2018/9/15.
 * Desc:
 */
public class NameViewModel extends ViewModel {

    //创建一个持有String类型数据的LiveData
    private MutableLiveData<String> mCurrentName;

    public MutableLiveData<String> getCurrentName() {
        if (mCurrentName == null) {
            mCurrentName = new MutableLiveData<>();
        }
        return mCurrentName;
    }
}
