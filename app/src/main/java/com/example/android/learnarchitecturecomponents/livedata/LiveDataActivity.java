package com.example.android.learnarchitecturecomponents.livedata;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.android.learnarchitecturecomponents.R;

public class LiveDataActivity extends AppCompatActivity {


    private final String TAG = getClass().getSimpleName();
    private NameViewModel model;

    public static void launch(Context context) {
        Intent intent = new Intent(context, LiveDataActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_data);
        model = ViewModelProviders.of(this).get(NameViewModel.class);
        final Observer<String> observer = new Observer<String>() {
            @Override
            public void onChanged(@Nullable String newName) {
                // Update the UI, in this case, a TextView.
                Log.e(TAG, "onChanged: " + newName);
            }
        };
        model.getCurrentName().observe(this, observer);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnUpdateLiveData:
                LiveData<String> data = Transformations.map(model.getCurrentName(), new Function<String, String>() {
                    @Override
                    public String apply(String input) {
                        return "map result " + input;
                    }
                });
                LiveData<String> data1 = Transformations.switchMap(model.getCurrentName(), new Function<String, LiveData<String>>() {
                    @Override
                    public LiveData<String> apply(String input) {
                        return null;
                    }
                });
                model.getCurrentName().setValue("hello world");
                break;
        }
    }

}
