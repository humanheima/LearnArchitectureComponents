# LiveDataBus 实现原理

根本原理：只需要在注册一个新的订阅者的时候把  LifecycleBoundObserver 的 mLastVersion 设置成跟LiveData的 mVersion 一致即可。
[Android消息总线的演进之路：用LiveDataBus替代RxBus、EventBus](https://tech.meituan.com/2018/07/26/android-livedatabus.html)

# LiveData 源码分析


### 话术

1. 在 Activity 的 onCreate 方法中，订阅 LiveData。传入了  LifecycleOwner 和 Observer。
   LiveData 内部，将 传入的  LifecycleOwner 和 Observer 封装成一个 LifecycleBoundObserver。
2. 使用 LifecycleBoundObserver 订阅 LifecycleOwner 的 Lifecycle (生命周期)事件。
3. 当 LifecycleOwner 的 Lifecycle (生命周期)事件 到达 CREATED 时，不活跃，不做任何处理。
3. 当 LifecycleOwner 的 Lifecycle (生命周期)事件 到达 STARTED 时，LiveData.LifecycleBoundObserver 会收到 onStateChanged 回调, LifecycleBoundObserver 活跃状态 mActive 置为 true。
4. LifecycleBoundObserver 判断，如果 LifecycleOwner 的 Lifecycle (生命周期)状态，如果是销毁状态，就移除观察者。
5. 如果 LifecycleOwner 的 Lifecycle (生命周期)状态，如果是活跃状态(至少是 STARTED 状态)，LifecycleBoundObserver 就调用 dispatchingValue 方法，通知观察者。
6. 在通知的时候，如果观察者的  mLastVersion 大于等于 LiveData 的 mVersion (observer.mLastVersion >= mVersion) 了，例如 LiveData 开始值为null的时候，就不通知了。如果一上来就要通知，那么可以使用 LiveData 的非空构造函数，直接传递一个值，构建 LiveData对象。
7. 到达 RESUMED 时，LiveData.LifecycleBoundObserver 会收到 onStateChanged 回调。这个时候 LifecycleBoundObserver 活跃状态 mActive true。新计算出来的  LifecycleBoundObserver 活跃状态 mActive 还是为true，不需要通知。


### 粘性事件


LiveData 的粘性特性是指新观察者在订阅时会收到最近一次更新的数据。这是通过 mVersion 和观察者的 mLastVersion 比较实现的：

1. 首先创建的 LifecycleBoundObserver 观察 LifeCycleOwner 的生命周期状态的时候。LifecycleBoundObserver内部会被封装成一个 ObserverWithState。
2. ObserverWithState 初始状态是 INITIALIZED。LifeCycleOwner 生命周期状态 targetState 比如是 RESUMED ，是 大于 INITIALIZED。就会主动通知 LifecycleBoundObserver 调用 dispatchingValue 方法，通知观察者。

### LiveData 通知调用栈

从 onCreate 方法中订阅，当Activity 生命周期到达 start 的时候，LiveData.LifecycleBoundObserver 收到 STARTED 事件(event)，这个时候 调用 dispatchingValue 方法，通知观察者。

```txt
onChanged: Hello world
                                                                   java.lang.Throwable
                                                                  	at com.example.architecturecomponents.livedata.LiveDataActivity.onCreate$lambda$0(LiveDataActivity.kt:52)
                                                                  	at com.example.architecturecomponents.livedata.LiveDataActivity.lambda$c6ppBwbY_Y-TOvQ6aqZ-STq4PrQ(Unknown Source:0)
                                                                  	at com.example.architecturecomponents.livedata.-$$Lambda$LiveDataActivity$c6ppBwbY_Y-TOvQ6aqZ-STq4PrQ.onChanged(Unknown Source:4)
                                                                  	at androidx.lifecycle.LiveData.considerNotify(LiveData.java:133)
                                                                  	at androidx.lifecycle.LiveData.dispatchingValue(LiveData.java:146)
                                                                  	at androidx.lifecycle.LiveData$ObserverWrapper.activeStateChanged(LiveData.java:468)
                                                                  	at androidx.lifecycle.LiveData$LifecycleBoundObserver.onStateChanged(LiveData.java:425)
                                                                  	at androidx.lifecycle.LifecycleRegistry$ObserverWithState.dispatchEvent(LifecycleRegistry.java:354)
                                                                  	at androidx.lifecycle.LifecycleRegistry.forwardPass(LifecycleRegistry.java:265)
                                                                  	at androidx.lifecycle.LifecycleRegistry.sync(LifecycleRegistry.java:307)
                                                                  	at androidx.lifecycle.LifecycleRegistry.moveToState(LifecycleRegistry.java:148)
                                                                  	at androidx.lifecycle.LifecycleRegistry.handleLifecycleEvent(LifecycleRegistry.java:134)
                                                                  	at androidx.lifecycle.ReportFragment.dispatch(ReportFragment.java:68)
                                                                  	at androidx.lifecycle.ReportFragment$LifecycleCallbacks.onActivityPostStarted(ReportFragment.java:187)
                                                                  	at android.app.Activity.dispatchActivityPostStarted(Activity.java:1423)
                                                                  	at android.app.Activity.performStart(Activity.java:8404)
                                                                  	at android.app.ActivityThread.handleStartActivity(ActivityThread.java:3669)
                                                                  	at android.app.servertransaction.TransactionExecutor.performLifecycleSequence(TransactionExecutor.java:224)
                                                                  	at android.app.servertransaction.TransactionExecutor.cycleToPath(TransactionExecutor.java:204)
                                                                  	at android.app.servertransaction.TransactionExecutor.executeLifecycleState(TransactionExecutor.java:176)
                                                                  	at android.app.servertransaction.TransactionExecutor.execute(TransactionExecutor.java:97)
                                                                  	at android.app.ActivityThread$H.handleMessage(ActivityThread.java:2306)
                                                                  	at android.os.Handler.dispatchMessage(Handler.java:106)
                                                                  	at android.os.Looper.loopOnce(Looper.java:201)
                                                                  	at android.os.Looper.loop(Looper.java:288)
                                                                  	at android.app.ActivityThread.main(ActivityThread.java:7918)
                                                                  	at java.lang.reflect.Method.invoke(Native Method)
                                                                  	at com.android.internal.os.RuntimeInit$MethodAndArgsCaller.run(RuntimeInit.java:548)
                                                                  	at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:936)

```



# Grok 的回答


我来为你详细解析 Android 中 LiveData 的原理。LiveData 是 Android Jetpack 组件库中的一种生命周期感知型数据持有类，广泛用于 MVVM 架构中，用于在 UI 层和数据层之间实现响应式数据传递。以下是对其原理的逐步拆解：

---

### 1. **LiveData 的核心概念**
LiveData 是一个可观察的数据持有者，它能够感知组件（如 Activity 或 Fragment）的生命周期状态，并在适当的时机（如组件处于活跃状态时）通知观察者更新数据。它的核心特点包括：
- **生命周期感知**：只在组件处于 STARTED 或 RESUMED 状态时通知观察者，避免内存泄漏或崩溃。
- **粘性事件**：新订阅者可以收到订阅前最后一次更新的数据。
- **线程安全**：支持在主线程和后台线程中更新数据。

LiveData 的典型使用场景是通过 ViewModel 将数据暴露给 UI，并在数据变化时自动更新界面。

---

### 2. **LiveData 的内部实现**
LiveData 的源码主要集中在 `androidx.lifecycle.LiveData` 类中，以下是其关键实现原理：

#### 2.1 **数据持有与观察者模式**
LiveData 内部通过一个泛型字段 `mData` 来存储数据，并使用 `Observer` 接口实现观察者模式。核心代码结构如下：
```java
public abstract class LiveData<T> {
    private final Object mDataLock = new Object();
    private T mData; // 存储的数据
    private int mVersion; // 数据版本号，用于追踪变化
    private final Map<Observer<? super T>, ObserverWrapper> mObservers = new HashMap<>(); // 观察者集合
    // ...
}
```
- `mData`：保存当前数据的值。
- `mObservers`：存储所有观察者及其包装类（ObserverWrapper），以便在数据变化时通知它们。
- `mVersion`：记录数据更新的版本号，用于区分新旧数据，避免重复通知。

#### 2.2 **生命周期绑定**
LiveData 通过 `LifecycleOwner` 和 `Lifecycle` 来感知组件的生命周期。观察者订阅时需要传入 `LifecycleOwner`，LiveData 会根据其状态决定是否分发数据：
```java
public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
    if (owner.getLifecycle().getCurrentState() == DESTROYED) {
        return; // 如果组件已销毁，不添加观察者
    }
    LifecycleBoundObserver wrapper = new LifecycleBoundObserver(owner, observer);
    mObservers.put(observer, wrapper);
    owner.getLifecycle().addObserver(wrapper);
}
```
- `LifecycleBoundObserver` 是观察者的包装类，它实现了 `LifecycleEventObserver`，监听生命周期事件。
- 当组件状态变为 DESTROYED 时，LiveData 会自动移除相关观察者，避免内存泄漏。

#### 2.3 **数据分发机制**
LiveData 的数据更新通过 `setValue`（主线程）或 `postValue`（后台线程）触发：
- **`setValue`**：直接更新数据并通知活跃的观察者。
```java
protected void setValue(T value) {
    synchronized (mDataLock) {
        mData = value;
        mVersion++;
    }
    dispatchingValue(null); // 通知所有活跃观察者
}
```
- **`postValue`**：将更新任务发送到主线程执行。
```java
protected void postValue(T value) {
    synchronized (mDataLock) {
        mPendingData = value; // 暂存数据
    }
    ArchTaskExecutor.getInstance().postToMainThread(mPostValueRunnable); // 主线程执行
}
```
通知逻辑由 `dispatchingValue` 方法实现，它会遍历 `mObservers`，检查观察者对应的生命周期状态，仅通知活跃的观察者（STARTED 或 RESUMED）。

#### 2.4 **粘性事件的处理**
LiveData 的粘性特性是指新观察者在订阅时会收到最近一次更新的数据。这是通过 `mVersion` 和观察者的 `mLastVersion` 比较实现的：
- 每次数据更新，`mVersion` 递增。
- 观察者初次订阅时，如果其 `mLastVersion` 小于当前 `mVersion`，就会收到 `mData` 的最新值。

如果不希望粘性行为，可以使用扩展库中的 `SingleLiveEvent` 或自定义实现。

---

### 3. **关键工作流程**
1. **订阅**：
    - 调用 `observe()`，传入 `LifecycleOwner` 和 `Observer`。
    - LiveData 检查生命周期状态并将观察者加入 `mObservers`。
2. **数据更新**：
    - 通过 `setValue` 或 `postValue` 更新数据。
    - `mVersion` 增加，触发通知。
3. **通知观察者**：
    - 检查每个观察者的生命周期状态。
    - 如果活跃，调用 `observer.onChanged()`。
4. **销毁**：
    - 组件销毁时，`Lifecycle` 通知 LiveData 移除观察者。

---

### 4. **优点与局限性**
#### 优点：
- **生命周期安全**：避免因组件销毁导致的异常。
- **简单易用**：与 ViewModel 配合，实现响应式编程。
- **线程灵活**：支持主线程和后台线程操作。

#### 局限性：
- **粘性事件问题**：可能导致新观察者收到不需要的历史数据。
- **单一数据流**：不像 Flow 或 RxJava 那样支持复杂的操作符。

---

### 5. **源码中的关键点补充**
- **ObserverWrapper**：抽象层，负责管理观察者的状态（如活跃状态、版本号）。
- **ArchTaskExecutor**：用于线程切换，确保数据更新在主线程执行。
- **hook 机制**：LiveData 使用反射（早期版本）或编译时注解（新版本）优化性能。

---

### 6. **总结**
LiveData 的核心原理是通过观察者模式、生命周期感知和线程安全的机制，实现数据与 UI 的高效同步。它简化了 Android 开发中的状态管理，是 MVVM 架构的重要组成部分。如果你需要更深入的源码分析或具体示例代码，可以告诉我，我会进一步展开！