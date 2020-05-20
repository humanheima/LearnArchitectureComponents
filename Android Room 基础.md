本文学习Room的基本使用，参考[Android Room with a View - Kotlin](https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/index.html?index=..%2F..index#0)
这篇文章。

Room简介：Room 持久性库在 SQLite 的基础上提供了一个抽象层，让用户能够在充分利用 SQLite 的强大功能的同时，获享更强健的数据库访问机制。

前提条件：要在项目中配置好Kotlin，要了解一点Kotlin协程的知识。

添加依赖

```
dependencies {
  
  def room_version = "2.2.5"

  implementation "androidx.room:room-runtime:$room_version"
  kapt "androidx.room:room-compiler:$room_version" 

  // Room的Kotlin扩展和对协程的支持
  implementation "androidx.room:room-ktx:$room_version"
  
}
    
```

配置编译器选项

Room 具有以下注释处理器选项：
* room.schemaLocation：配置并启用将数据库架构导出到给定目录中的 JSON 文件的功能。
* room.incremental：启用 Gradle 增量注释处理器。
* room.expandProjection：配置 Room 以重新编写查询，使其顶部星形投影在展开后仅包含 DAO 方法返回类型中定义的列。


在 app 的 build.gradle 文件中，配置schemaLocation选项为`app/schemas`，方便我们查看数据库架构信息。
```html
 defaultConfig {
        ...
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = ["room.schemaLocation":
                                     "$projectDir/schemas".toString()]
            }
        }

    }
```

### 创建一个实体类，对应数据库中的一张表

```kotlin
@Entity(tableName = "word_table")
data class Word(
        @PrimaryKey @ColumnInfo(name = "word") val word: String,
        val name: String? = null
)
```
* @Entity：注解的类对应数据库中的一张表，我们可以指定表名，如果不指定的话，默认是类的名字。
* @PrimaryKey：每一个表都需要一个主键，这点需要注意，Room就是根据主键是否相同来判断是否是同一个对象。
* @ColumnInfo：指定类的属性在表中列的名字，如果不指定，默认就是属性名。
* 存储在数据库中的类属性可见性必须是public的。

### 创建DAO（data access object）

DAO必须是接口或者抽象类，Room使用注解帮我们生成访问数据库的代码，感觉和Retrofit有类似之处。接下来我们创建一个DAO类，具有简单的增删改查的功能。

```kotlin
//注释1处
@Dao
interface WordDao {


    @Insert()
    suspend fun insert(word: Word)
    
    @Delete
    suspend fun delete(word: Word):Int

    @Update
    suspend fun update(word: Word):Int

    /**
     * 真正的查询默认是在工作线程的，Room内部为我们做了一些线程切换的工作，暂时不去管
     */
    @Query("SELECT * FROM word_table ORDER BY word ASC")
    fun getAlphabetizedWords(): LiveData<List<Word>>
    
    //...
}
```

注释1处，使用@Dao注解WordDao类，增删改方法我们都用suspend关键字修饰了，所以你需要在协程中或者另一个挂起函数中调用。

### 添加Room数据库

```kotlin
//注释1处
@Database(entities = [Word::class], version = 1, exportSchema = true)
abstract class WordRoomDatabase : RoomDatabase() {

    //注释2处
    abstract fun wordDao(): WordDao

    //获取单例
    companion object {

        @JvmStatic
        @Volatile
        private var INSTANCE: WordRoomDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): WordRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                //注释3处
                val instance = Room.databaseBuilder(context.applicationContext, WordRoomDatabase::class.java,
                        "word_database")
                        .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}
```

Room数据库必须是一个继承自RoomDatabase的抽象类。通常情况下应用内应该只有一个Room数据库实例。  
注释1处使用@Database注解WordRoomDatabase类，并指定数据库中的表，版本号和是否在指定的schemaLocation目录下输出数据库架构信息。

注释2处，提供获取DAO类的抽象方法。

注释3处，获取WordRoomDatabase类的实例。

### 插入数据

```
//在协程中调用
launch {
     val word = Word("Hi", "dmw")
     wordDAO.insert(word)
}
```

如果要插入多条数据，我们可以如下声明
```
//插入两条数据
@Insert
suspend fun insert(word1: Word, word2: Word)

//可变参数
@Insert
fun insert(vararg words: Word)

//插入列表
@Insert
suspend fun insert(wordList: List<Word>)

```
#### 插入数据时候的冲突策略

如果我们插入的数据在数据库表中已经存在了，那么我们就会遇到冲突。遇到冲突时候的时候处理冲突的策略如下：

```java

public @interface OnConflictStrategy {
    /**
     * 替换，数据会发生变化
     */
    int REPLACE = 1;
   
    /**
     * 默认行为，终止操作，抛入异常
     */
    int ABORT = 3;
    
    /**
     * 忽略冲突，数据不会发生变化
     */
    int IGNORE = 5;

}

```

### 删除数据

```kotlin
private fun deleteWord() {
    launch {
        val word = Word("Hi", "dmw")
        //返回删除的行数
        val row = wordDAO.delete(word)

        Log.d(TAG, "deleteWord: row = $row")

    }
}
```

删除多条数据可如下声明
```
@Delete
suspend fun delete(word1: Word, word2: Word): Int

//可变参数
@Insert
fun delete(vararg words: Word)

//插入列表
@Delete
suspend fun delete(wordList: List<Word>)

```

### 更新数据

注意在更新数据的时候，是根据`primary key`来查找要更新的记录。如果记录不存在，则不会改变数据。

```kotlin
private fun updateWord() {
    launch {
        val word1 = Word("Hi0", "dmw${System.currentTimeMillis()}")
        //更新的行数
        val row = wordDAO.update(word1)
        Log.d(TAG, "updateWord: row = $row")
    }
}
```

更新多条记录可如下声明
```
@Update
suspend fun update(word1: Word, word2: Word): Int

@Update
suspend fun update(vararg words: Word): Int

@Update
suspend fun update(wordList: List<Word>): Int

```

### 查询数据

```

/**
 * 真正的查询默认是在工作线程的，Room内部为我们做了一些线程切换的工作，暂时不去管
 */
@Query("SELECT * FROM word_table ORDER BY word ASC")
fun getAlphabetizedWords(): LiveData<List<Word>>

```
默认情况下，为了避免阻塞主线程，Room不允许在主线程查询数据。当Room的返回类型是`LiveData`的时候，查询操作会自动在一个后台线程异步进行。

```kotlin
private fun queryWord() {
    val allWords = wordDAO.getAlphabetizedWords()
    allWords.observe(this, Observer { list ->
        list.forEach {
            Log.d(TAG, "queryWord: ${it.word},${it.name}")
        }
    })
}
```





























