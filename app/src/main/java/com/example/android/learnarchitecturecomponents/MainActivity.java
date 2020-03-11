package com.example.android.learnarchitecturecomponents;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.learnarchitecturecomponents.room.RoomFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RoomFragment roomFragment = RoomFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.fl_container, roomFragment).commit();
    }
}
