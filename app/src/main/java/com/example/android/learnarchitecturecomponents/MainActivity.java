package com.example.android.learnarchitecturecomponents;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RoomFragment roomFragment = RoomFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.fl_container, roomFragment).commit();
    }
}
