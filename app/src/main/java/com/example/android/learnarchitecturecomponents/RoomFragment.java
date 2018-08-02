package com.example.android.learnarchitecturecomponents;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.learnarchitecturecomponents.databinding.RoomFragmentBind;
import com.example.android.learnarchitecturecomponents.entities.Address;
import com.example.android.learnarchitecturecomponents.entities.NameTuple;
import com.example.android.learnarchitecturecomponents.entities.User;

import java.util.List;

public class RoomFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "RoomFragment";
    private RoomFragmentBind binding;

    public RoomFragment() {
        // Required empty public constructor
    }

    public static RoomFragment newInstance() {
        RoomFragment fragment = new RoomFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_room, container, false);
        binding.btnInsert.setOnClickListener(this);
        binding.btnQuery.setOnClickListener(this);
        binding.btnUpdate.setOnClickListener(this);
        binding.btnDelete.setOnClickListener(this);
        return binding.getRoot();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_insert:
                insert();
                break;
            case R.id.btn_query:
                query();
                break;
            case R.id.btn_update:
                update();
                break;
            case R.id.btn_delete:
                delete();
                break;
            default:
                break;
        }
    }

    private void delete() {
        App.getExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Address address = new Address("国泰路", "徐汇区", "上海", 21110);
                User user = new User("hongmin", "du", address);
                user.setId(4);
                int rowAffected = App.getDataBase().userDao().deleteUsers(user);
                Log.d(TAG, "run: rowAffected=" + rowAffected);
            }
        });
    }

    private void update() {
        App.getExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Address address = new Address("国泰路", "徐汇区", "上海", 21110);
                User user = new User("hongmin", "du", address);
                user.setId(4);
                int rowAffected = App.getDataBase().userDao().updateUsers(user);
                Log.d(TAG, "run: rowAffected=" + rowAffected);
            }
        });
    }

    private void query() {
        App.getExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<NameTuple> nameTuples = App.getDataBase().userDao().loadFullName();
                for (NameTuple nameTuple : nameTuples) {
                    Log.d(TAG, "run: " + nameTuple.toString());
                }
            }
        });
    }

    private void insert() {
        App.getExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Address address = new Address("国泰路", "杨浦区", "上海", 21610);
                User user = new User("hongmin", "du", address);
                long id = App.getDataBase().userDao().insertUser(user);
                Log.d(TAG, "insert: id= " + id);

            }
        });
    }
}
