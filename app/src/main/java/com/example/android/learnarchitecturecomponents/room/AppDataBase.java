package com.example.android.learnarchitecturecomponents.room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.learnarchitecturecomponents.room.dao.FruitDao;
import com.example.android.learnarchitecturecomponents.room.dao.UserDao;
import com.example.android.learnarchitecturecomponents.room.entities.Fruit;
import com.example.android.learnarchitecturecomponents.room.entities.User;

/**
 * Created by DuMingwei on 2018/8/2.
 * Description:
 */
@Database(entities = {User.class, Fruit.class}, version = 3)
public abstract class AppDataBase extends RoomDatabase {

    private static final String TAG = AppDataBase.class.getSimpleName();

    private static final String DATABASE_NAME = "basic-sample-db";
    private static AppDataBase instance;

    public abstract UserDao userDao();

    public abstract FruitDao fruitDao();

    public static AppDataBase getInstance(Context context, final AppExecutors executors) {
        if (instance == null) {
            synchronized (AppDataBase.class) {
                if (instance == null) {
                    instance = buildDataBase(context.getApplicationContext(), executors);
                }
            }
        }
        return instance;
    }

    private static AppDataBase buildDataBase(Context applicationContext, AppExecutors executors) {
        return Room.databaseBuilder(applicationContext, AppDataBase.class, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Log.d(TAG, "buildDataBase onCreate: ");
                    }
                }).build();
    }

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER NOT NULL, `name` TEXT, `price` REAL NOT NULL,PRIMARY KEY(`id`))");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE User "
                    + " ADD COLUMN job TEXT");
        }
    };
}
