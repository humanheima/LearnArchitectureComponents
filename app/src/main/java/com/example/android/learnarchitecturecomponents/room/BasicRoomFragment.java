package com.example.android.learnarchitecturecomponents.room;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import com.example.android.learnarchitecturecomponents.App;
import com.example.android.learnarchitecturecomponents.R;
import com.example.android.learnarchitecturecomponents.databinding.RoomFragmentBind;
import com.example.android.learnarchitecturecomponents.room.entities.Address;
import com.example.android.learnarchitecturecomponents.room.entities.Fruit;
import com.example.android.learnarchitecturecomponents.room.entities.User;

import java.util.List;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Crete by dumingwei on 2020-03-12
 * Desc: Basic room use
 */
public class BasicRoomFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "BasicRoomFragment";
    private RoomFragmentBind binding;

    public BasicRoomFragment() {
        // Required empty public constructor
    }

    public static BasicRoomFragment newInstance() {
        BasicRoomFragment fragment = new BasicRoomFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_baisc_room, container, false);
        binding.btnInsert.setOnClickListener(this);
        binding.btnQuery.setOnClickListener(this);
        binding.btnUpdate.setOnClickListener(this);
        binding.btnDelete.setOnClickListener(this);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findAllFruit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_insert:
                //insert();
                insertFruit();
                break;
            case R.id.btn_query:
                //query();
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
                User user = new User("dumingwei", "du", address);
                user.setId(1);
                int rowAffected = App.getDataBase().userDao().updateUsers(user);
                Log.d(TAG, "run: rowAffected=" + rowAffected);
            }
        });
    }

    @SuppressLint("CheckResult")
    private void query() {
       /* App.getExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<NameTuple> nameTuples = App.getDataBase().userDao().loadFullName();
                for (NameTuple nameTuple : nameTuples) {
                    Log.d(TAG, "run: " + nameTuple.toString());
                }
            }
        });*/

        App.getDataBase().userDao().findUserMaybeWithId(1L)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        Log.d(TAG, "accept: " + user.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: " + throwable.getMessage());
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        Log.d(TAG, "run: complete");
                    }
                });

        /*App.getDataBase().userDao().getUserById(1L)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(new Consumer<User>() {
                    @Override
                    public void accept(User user) throws Exception {
                        Log.d(TAG, "accept: " + user.toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "accept: " + throwable.getMessage());
                    }
                });*/

    }

    private void insert() {
        App.getExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Address address = new Address("国泰路", "杨浦区", "上海", 21610);
                User user = new User("hongmin", "du", address);
                user.setJob("android develop");
                long id = App.getDataBase().userDao().insertUser(user);
                Log.d(TAG, "insert: id= " + id);
            }
        });
    }

    private void insertFruit() {
        App.getExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Fruit fruit = new Fruit("苹果", 5D);
                long rowId = App.getDataBase().fruitDao().insertFruit(fruit);
                Log.d(TAG, "insertFruit: " + rowId);
            }
        });
    }

    private void findAllFruit() {
        App.getExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                App.getDataBase().fruitDao().getFruits().observe(getActivity(), new Observer<List<Fruit>>() {
                    @Override
                    public void onChanged(@Nullable List<Fruit> fruitList) {
                        for (Fruit fruit : fruitList) {
                            Log.d(TAG, "onChanged: " + fruit.toString());
                        }
                    }
                });
            }
        });
    }


}
