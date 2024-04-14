package com.example.architecturecomponents.livedata;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.arch.core.util.Function;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModelProvider;
import com.example.architecturecomponents.R;
import com.example.architecturecomponents.databinding.ActivityLiveDataBinding;

/**
 * Crete by dumingwei on 2020-03-11
 * Desc: 学习LiveData
 */
public class LiveDataActivity extends AppCompatActivity {


    private final String TAG = getClass().getSimpleName();
    private NameViewModel model;
    private Observer<String> foreverObserver;


    public static void launch(Context context) {
        Intent intent = new Intent(context, LiveDataActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: this" + this);
        super.onCreate(savedInstanceState);
        ActivityLiveDataBinding binding = DataBindingUtil.<ActivityLiveDataBinding>setContentView(this,
                R.layout.activity_live_data);
        model = new ViewModelProvider(this).get(NameViewModel.class);
        final Observer<String> observer = new Observer<String>() {
            @Override
            public void onChanged(@Nullable String newName) {
                // Update the UI, in this case, a TextView.
                Log.e(TAG, "onChanged: " + newName);
            }
        };

        model.getCurrentName().setValue("Hello world");
        binding.setModel(model);
        binding.setLifecycleOwner(this);

        //观察者要和LifecycleOwner
        model.getCurrentName().observe(this, observer);

        foreverObserver = new Observer<String>() {
            @Override
            public void onChanged(@Nullable String newName) {
                // Update the UI, in this case, a TextView.
                Log.e(TAG, "foreverObserver onChanged: " + newName);
            }
        };

        /**
         * 永久观察，不需要关联LifecycleOwner，注意要在适当的时候调用removeObserver(Observer)来移除这些观察者
         */
        model.getCurrentName().observeForever(foreverObserver);

    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnManualRecreateActivity:
                recreate();
                break;
            case R.id.btnUpdateLiveData:
                LiveData<String> data = Transformations.map(model.getCurrentName(), new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return "map result " + input;
                    }
                });
                LiveData<String> data1 = Transformations.switchMap(model.getCurrentName(),
                        new Function<String, LiveData<String>>() {
                            @Override
                            public LiveData<String> apply(String input) {
                                return null;
                            }
                        });
                model.getCurrentName().setValue("name发生了改变" + System.currentTimeMillis());
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        model.getCurrentName().removeObserver(foreverObserver);

    }
}
