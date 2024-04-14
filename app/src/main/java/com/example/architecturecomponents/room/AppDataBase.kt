package com.example.architecturecomponents.room

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.architecturecomponents.room.dao.FruitDao
import com.example.architecturecomponents.room.dao.WordDao
import com.example.architecturecomponents.room.entities.Word
import kotlinx.coroutines.CoroutineScope

/**
 * Created by DuMingwei on 2018/8/2.
 * Description:
 */
@Database(
    entities = [com.example.architecturecomponents.room.entities.User::class, com.example.architecturecomponents.room.entities.Fruit::class, Word::class],
    version = 4,
    exportSchema = true
)
abstract class AppDataBase : RoomDatabase() {

    abstract fun userDao(): com.example.architecturecomponents.room.dao.UserDao

    abstract fun fruitDao(): FruitDao

    abstract fun wordDAO(): WordDao

    companion object {

        private val TAG = AppDataBase::class.java.simpleName

        private val DATABASE_NAME = "basic-sample-db"

        @Volatile
        private var INSTANCE: AppDataBase? = null

        @JvmStatic
        fun getInstance(context: Context, scope: CoroutineScope): AppDataBase {
            return INSTANCE ?: synchronized(AppDataBase::class.java) {
                val instance = buildDataBase(context.applicationContext, scope)
                INSTANCE = instance
                return instance
            }
        }

        private fun buildDataBase(applicationContext: Context, scope: CoroutineScope): AppDataBase {
            return Room.databaseBuilder(applicationContext, AppDataBase::class.java, DATABASE_NAME)
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_2_3,
                    MIGRATION_3_4
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        Log.d(TAG, "buildDataBase onCreate: ")
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        Log.d(TAG, "onOpen: ")
                    }
                })
                //.addCallback(WordDatabaseCallback(scope))
                .build()
        }

        private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER NOT NULL, `name` TEXT, `price` REAL NOT NULL,PRIMARY KEY(`id`))")
            }
        }

        private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE User " + " ADD COLUMN job TEXT")
            }
        }
        private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE `word_table` (`word` TEXT)"
                )
            }
        }
    }


}



