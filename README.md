##Learn android architecture components

###Room

在 build.gradle 文件中，配置schema
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

**只查询某些列，但是这个方法还有待测试，因为查出来的结果条数总是少**
```java
/**
     * 只查询某些列,查出来的数据条数不对,总是会少
     *
     * @return
     */
    @Query("SELECT first_name,lastName FROM user")
    List<NameTuple> loadFullName();
    
App.getExecutors().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<NameTuple> nameTuples = App.getDataBase().userDao().loadFullName();
                for (NameTuple nameTuple : nameTuples) {
                    Log.d(TAG, "run: " + nameTuple.toString());
                }
            }
        });

```
**配合RxJava2使用**
```java
@Query("SELECT * FROM user WHERE id=:id")
Single<User> findUserWithId(long id);

  App.getDataBase().userDao().findUserWithId(1L)
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
                  });
  
  
  /** 使用 Flowable
       * 1. 当表中没有记录的时候，Flowable 什么都不发射
       * 2.当表中有一条记录的时候，触发onNext方法
       * 3. 当此条记录被更新的时候，Flowable会自动发射触发onNext方法
       *
       * @param userId
       * @return
       */
      @Query("SELECT * FROM user WHERE id = :userId")
      Flowable<User> getUserById(long userId);
      
      App.getDataBase().userDao().getUserById(1L)
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
                      });

  
```
**和LiveData配合使用，等待添加**

**数据库迁移**
1. 新加一个表
* 新建相应的实体类`Fruit`，并用`@Entity`注解
* 在`AppDataBase`类的注解中，加入新的实体类@Database(entities = {User.class, Fruit.class}, version = 2),并把version加1
创建MIGRATION_1_2 并应用

```java

static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE `Fruit` (`id` INTEGER NOT NULL, `name` TEXT, `price` REAL NOT NULL,PRIMARY KEY(`id`))");
        }
    };

  return Room.databaseBuilder(applicationContext, AppDataBase.class, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2)
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        Log.d(TAG, "buildDataBase onCreate: ");
                    }
                }).build();


```
注意：
1. double 和 float类型对应表中列的类型为REAL 
2. 注意 NOT NULL 的使用

2. 修改表，给User表增加一列，职业(job)
* 给User表增加一个字段和对应的getter,setter
```
private String job;
public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

```
* 在`AppDataBase`类的注解中，把version加1

* 创建MIGRATION_2_3并应用

## LiveData
LiveData是一个可观察的数据持有类。和常规的可观察者不一样，LiveData具有生命感知能力，意味着LiveData
遵循其他应用组件，例如Activity，Fragment或者Service的生命周期。这种感知能力确保LiveData只会更新那些
处于生命周期活动状态的应用组件观察者。
引入LiveData组件
```java
def lifecycle_version = "1.1.1"
implementation "android.arch.lifecycle:livedata:$lifecycle_version"
```
LiveData 认为一个观察者处于生命周期的 `STARTED`或者`RESUMED`状态的时候，是处于活动状态。LiveData的
更新信息仅通知处于活动状态的观察者。处于非活动状态的观察者不会收到通知。

你可以在注册一个观察者的时候同时传入一个实现了`LifecycleOwner`接口的对象。

```java
@MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        owner.getLifecycle().addObserver(wrapper);
    }
```
这样做的好处是当 `owner`处于生命周期 `DESTORYED`状态的时候，LiveData可以移除对应的 `observer`。
这种方式对于activities和fragments来说特别有用，因为这样可以安全的定语LiveData对象而不用担心内存泄漏。
因为当activities和fragments处于生命周期`DESTORYED`状态的时候会立即取消订阅。

使用LiveData的优势
* 确保你的UI和你的数据状态一致
 LiveData遵循观察者模式。当观察者的生命周期改变的时候，LiveData会通知观察者。



