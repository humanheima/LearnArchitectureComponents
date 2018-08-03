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
