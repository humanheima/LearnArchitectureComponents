本篇文章下学习一下Lifecycle，先混个脸熟。相关的几个类

```java
androidx.lifecycle.Lifecycle
androidx.lifecycle.LifecycleOwner
//枚举类型
androidx.lifecycle.Lifecycle.Event
//枚举类型
androidx.lifecycle.Lifecycle.State
androidx.lifecycle.LifecycleObserver
```



LifecycleOwner接口：定义了一个返回Lifecycle对象的方法。

```java
public interface LifecycleOwner {
 
    @NonNull
    Lifecycle getLifecycle();
    
}
```

`androidx.fragment.app.Fragment Fragment`和`androidx.fragment.app.FragmentActivity`都实现了此接口。

FragmentActivity继承了ComponentActivity，ComponentActivity实现了LifecycleOwner接口。

```
public class ComponentActivity extends androidx.core.app.ComponentActivity implements
        LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner, OnBackPressedDispatcherOwner {
        
    //生命周期状态信息
    private final LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);


    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        //返回生命周期状态信息信息
        return mLifecycleRegistry;
    }
    //...          
}
```

Lifecycle类：用于存储有关组件(如 Activity 或 Fragment)的生命周期状态的信息，并允许其他对象观察此状态。

```java
public abstract class Lifecycle {

    /**
     * 添加一个LifecycleObserver，当LifecycleOwner状态改变的时候会收到通知
     * <p>
     * 指定的观察者将被带到LifecycleOwner的当前状态。
     * 例如，如果LifecycleOwner处于STARTED状态，那么指定的观察者会收到ON_CREATE和ON_START事件。
     *
     * @param observer The observer to notify.
     */
    @MainThread
    public abstract void addObserver(@NonNull LifecycleObserver observer);

    /**
     * 移除指定的观察者
     * <p>
     * 当生命周期状态改变事件正在被分发的时候调用这个方法：
     * 1. 如果指定的观察者没有收到这个事件，那么它就不会收到事件了。
     * 2. 如果指定的观察者有超过一个方法在观察当前正在分发的事件并且这少有一个方法收到了事件，
     * 那么删除操作会在事件分发完毕以后进行。
     *
     * @param observer The observer to be removed.
     */
    @MainThread
    public abstract void removeObserver(@NonNull LifecycleObserver observer);

    /**
     * 返回Lifecycle的当前状态
     *
     * @return The current state of the Lifecycle.
     */
    @MainThread
    @NonNull
    public abstract State getCurrentState();

}
```

### 生命周期事件Lifecycle.Event

`Lifecycle.Event`是一个枚举类，定义了LifecycleOwner发出的所有事件类型

| 枚举值 |描述|
|----------|----------|
| ON_ANY   | 可以用来匹配LifecycleOwner的任何事件|
| ON_CREATE   |LifecycleOwner的onCreate事件|
| ON_DESTROY|LifecycleOwner的onDestroy事件|
| ON_PAUSE|LifecycleOwner的onPause事件|
| ON_RESUME|LifecycleOwner的onResume事件|
| ON_CREATE   |LifecycleOwner的onCreate事件|
| ON_START|LifecycleOwner的onStart事件|
| ON_STOP   |LifecycleOwner的onStop事件|


### 生命周期状态 Lifecycle.State

`Lifecycle.State`也是一个枚举类，定义了LifecycleOwner所有的生命周期状态。

`DESTROYED`：LifecycleOwner的销毁状态，到达这个状态之后，Lifecycle不会再发射任何生命周期事件。对于Activity来说，正好在Activity的onDestroy方法调用之前会到达这个状态。

`INITIALIZED`：LifecycleOwner的初始状态，对于Activity来说，当Activity实例被创建但是还没有调用onCreate()方法的时候处于此状态。

`CREATED`：LifecycleOwner的已创建状态。对于Activity来说有两种情况会到达这个状态。

1. 在onCreate()方法调用之后。
2. 刚好在onStop()方法调用之前。

`STARTED`：LifecycleOwner的已开始状态。对于Activity来说有两种情况会到达这个状态。

1. 在onStart()方法调用之后。
2. 刚好在onPause()方法调用之前。

`RESUMED`：LifecycleOwner的活动状态。对于Activity来说在onResume()方法调用之后到达这个状态。

Lifecycle.State有一个isAtLeast()方法用来当前Lifecycle.State对象的状态是否到达了指定的状态。

```java
/**
 * @param state State to compare with
 * @return 如果当前Lifecycle.State对象的状态大于等于指定的状态返回true。
 */
public boolean isAtLeast(@NonNull State state) {
    return compareTo(state) >= 0;
}
```

状态值大小顺序从大到小。

```
RESUMED > STARTED > CREATED > INITIALIZED > DESTROYED
```


ComponentActivity 类的成员变量 LifecycleRegistry 对象，LifecycleRegistry继承Lifecycle。

```java
private final LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);
```

LifecycleRegistry类的构造方法。

```java
//生命周期状态
private Lifecycle.State mState;

private LifecycleRegistry(@NonNull LifecycleOwner provider, boolean enforceMainThread) {
    mLifecycleOwner = new WeakReference<>(provider);
    //初始状态是INITIALIZED
    mState = INITIALIZED;
    mEnforceMainThread = enforceMainThread;
}
```

我们在 LifeCycleActivity 各个生命周期函数中打印一下Lifecycle的状态。


```java
override fun onStart() {
    super.onStart()
    Log.d(TAG, "onStart: lifecycle.currentState = ${lifecycle.currentState}")
}
//省略其他生命周期函数
```

输出结果
```
2024-04-15 17:36:57.888 LifeCycleActivity                D  onCreate: lifecycle.currentState = INITIALIZED
2024-04-15 17:36:57.889 LifeCycleActivity                D  onStart: lifecycle.currentState = CREATED
2024-04-15 17:36:57.889 LifeCycleActivity                D  onResume: lifecycle.currentState = STARTED
2024-04-15 17:37:00.367 LifeCycleActivity                D  onCreate: lifecycle.currentState = RESUMED
2024-04-15 17:37:15.686 LifeCycleActivity                D  onPause: lifecycle.currentState = STARTED
2024-04-15 17:37:16.149 LifeCycleActivity                D  onStop: lifecycle.currentState = CREATED
2024-04-15 17:37:16.150 LifeCycleActivity                D  onDestroy: lifecycle.currentState = DESTROYED

```

### 接下来我们看看 LifecycleRegistry 的生命周期状态值是怎么改变的。

先说一下结论：

1. 先回调 Activity 的 onCreate() 方法，然后改变 LifecycleRegistry 的状态为 CREATED。
2. 先回调 Activity 的 onStart() 方法，然后改变 LifecycleRegistry 的状态为 STARTED。
3. 先回调 Activity 的 onResume() 方法，然后改变 LifecycleRegistry 的状态为 RESUMED。
4. 从 onPause开始。先改变 LifecycleRegistry 的状态为 STARTED，然后回调 Activity 的 onPause() 方法。
5. 先改变 LifecycleRegistry 的状态为 CREATED，然后回调 Activity 的 onStop() 方法。
6. 先改变 LifecycleRegistry 的状态为 DESTROYED，然后回调 Activity 的 onDestroy() 方法。


LifecycleRegistry 的 handleLifecycleEvent() 方法。

```java
public void handleLifecycleEvent(@NonNull Lifecycle.Event event) {
    enforceMainThreadIfNeeded("handleLifecycleEvent");
    //注释1处
    moveToState(event.getTargetState());
}
```
注释1处，根据生命周期事件获取对应的状态。先调用 `event.getTargetState()` 方法获取对应的状态。再调用 `moveToState()` 方法。

Lifecycle.Event 的 getTargetState() 方法。

```java
public State getTargetState() {
    switch(this) {
        case ON_CREATE:
        case ON_STOP:
            return State.CREATED;
        case ON_START:
        case ON_PAUSE:
            return State.STARTED;
        case ON_RESUME:
            return State.RESUMED;
        case ON_DESTROY:
            return State.DESTROYED;
        case ON_ANY:
            break;
    }
    throw new IllegalArgumentException(this + " has no target state");
}

```

### Activity 的 performCreate() 方法

```java
final void performCreate(Bundle icicle, PersistableBundle persistentState) {

    dispatchActivityPreCreated(icicle);
    mCanEnterPictureInPicture = true;
    // initialize mIsInMultiWindowMode and mIsInPictureInPictureMode before onCreate
    final int windowingMode = getResources().getConfiguration().windowConfiguration
        .getWindowingMode();
    mIsInMultiWindowMode = inMultiWindowMode(windowingMode);
    mIsInPictureInPictureMode = windowingMode == WINDOWING_MODE_PINNED;
    restoreHasCurrentPermissionRequest(icicle);
    if(persistentState != null) {
        onCreate(icicle, persistentState);
    } else {
        //注释1处，调用Activity的onCreate生命周期方法
        onCreate(icicle);
    }
    mActivityTransitionState.readState(icicle);

    mVisibleFromClient = !mWindow.getWindowStyle().getBoolean(
        com.android.internal.R.styleable.Window_windowNoDisplay, false);
    mFragments.dispatchActivityCreated();
    mActivityTransitionState.setEnterActivityOptions(this,
        getActivityOptions());
    //注释2处，改变LifecycleRegistry的状态为 CREATED
    dispatchActivityPostCreated(icicle);
}

```

注释1处，先调用 Activity 的 onCreate() 方法。然后注释2处，改变 LifecycleRegistry 的状态为 CREATED。


### Activity 的 performStart() 方法

```java
final void performStart(String reason) {
   
    //注释1处，调用Activity的onStart生命周期方法
    mInstrumentation.callActivityOnStart(this);
    //...
    //注释2处，改变LifecycleRegistry的状态为 STARTED
    dispatchActivityPostStarted();
}
```

注释1处，调用 Activity 的 onStart() 方法。然后注释2处，改变 LifecycleRegistry 的状态为 STARTED。


### Activity 的 performResume() 方法

```java
final void performResume(boolean followedByPause, String reason) {


    //注释1处，调用Activity的onResume生命周期方法
    mInstrumentation.callActivityOnResume(this);
    //...
    //注释2处，改变LifecycleRegistry的状态为 RESUMED
    dispatchActivityPostResumed();
    Trace.traceEnd(Trace.TRACE_TAG_WINDOW_MANAGER);
}

```

注释1处，先调用 Activity 的 onResume() 方法。然后注释2处，改变 LifecycleRegistry 的状态为 RESUMED。

### Activity 的 performPause() 方法

```java
final void performPause() {
    //注释1处，
    dispatchActivityPrePaused();
    //..
    //注释2处，调用Activity的onPause生命周期方法
    onPause();
    mResumed = false;
    dispatchActivityPostPaused();
    //...
}
```

注释1处，先改变 LifecycleRegistry 的状态为 STARTED ，然后调用 Activity 的 onPause() 方法。


### Activity 的 performStop() 方法

```java
final void performStop(boolean preserveWindow, String reason) {
    //...
    if(!mStopped) {
        //注释1处，先改变 LifecycleRegistry 的状态
        dispatchActivityPreStopped();
        //...
        //注释2处，调用Activity的onStop生命周期方法
        mFragments.dispatchStop();

        mCalled = false;
        mInstrumentation.callActivityOnStop(this);
        mStopped = true;
        dispatchActivityPostStopped();
    }
}
```

注释1处，先改变 LifecycleRegistry 的状态为 CREATED ，然后调用 Activity 的 onStop() 方法。

### Activity 的 performDestroy() 方法

```java
final void performDestroy() {
      
        //注释1处，先改变 LifecycleRegistry 的状态为 DESTROYED
        dispatchActivityPreDestroyed();
        mDestroyed = true;
        mWindow.destroy();
        //注释2处，调用Activity的onDestroy生命周期方法
        mFragments.dispatchDestroy();
        onDestroy();
        
        dispatchActivityPostDestroyed();
    }
 
```

注释1处，先改变 LifecycleRegistry 的状态为 DESTROYED，然后调用 Activity 的 onDestroy() 方法。


### 下面我们看看如何订阅生命周期事件。

首先自定义观察者类实现LifecycleObserver接口，然后使用注解订阅感兴趣的事件即可。
```kotlin
class MyObserver : LifecycleObserver {

    private val TAG: String? = "MyObserver"

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate(owner: LifecycleOwner) {
        Log.d(TAG, "onCreate: ")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart(owner: LifecycleOwner) {
        Log.d(TAG, "onStart: ")

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume(owner: LifecycleOwner) {
        Log.d(TAG, "onResume: ")

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause(owner: LifecycleOwner) {
        Log.d(TAG, "onPause: ")

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop(owner: LifecycleOwner) {
        Log.d(TAG, "onStop: ")

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(owner: LifecycleOwner) {
        Log.d(TAG, "onDestroy: ")
    }

}
```

这里我们订阅了所有的生命周期事件。

我们在Activity中使用一下。

```kotlin
class LifeCycleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_life_cycle)
        Log.d(TAG, "onCreate: lifecycle.currentState = ${lifecycle.currentState}")

        lifecycle.addObserver(MyObserver())
    }
}
```
注意：订阅的时候可能会出现如下错误
```
Default interface methods are only supported starting with Android N (--min-api 24): 
void androidx.lifecycle.DefaultLifecycleObserver.onCreate(androidx.lifecycle.LifecycleOwner)
```
可以在app的build.gtadle文件中指定compileOptions来解决
```gradle
compileOptions {
    targetCompatibility = 1.7
    sourceCompatibility = 1.7
}
```
指定1.7或者1.8都可以。

Logcat输出如下
```
D/MyObserver: onCreate: 
D/MyObserver: onStart: 
D/MyObserver: onResume: 
```
然后我们点一下返回键，Logcat输出如下
```
D/MyObserver: onPause: 
D/MyObserver: onStop: 
D/MyObserver: onDestroy: 
```

以上就是简单的使用，接下来我们看一看LifecycleRegistry类。

```java
/**
 * Lifecycle的一个实现类，可以处理多个观察者。
 * Support Library中的Fragment和Activity使用了此类。自定义的LifecycleOwner类也可以直接使用该类。
 */
public class LifecycleRegistry extends Lifecycle {

    //当前生命周期状态
    private State mState;
    
    //是否正在添加观察者，大于0表示正在添加观察者
    private int mAddingObserverCounter = 0;
    //是否正在处理事件
    private boolean mHandlingEvent = false;
    //是否有新的事件发生
    private boolean mNewEventOccurred = false;
   //...

}
```
LifecycleRegistry类的handleLifecycleEvent()方法。

```java
/**
 * 设置当前的生命周期状态并通知生命周期事件观察者
 * <p>
 * 注意，如果当前状态与上次调用此方法时的状态相同，则调用此方法无效。
 *
 * @param event 收到的生命周期事件
 */
public void handleLifecycleEvent(@NonNull Lifecycle.Event event) {
    enforceMainThreadIfNeeded("handleLifecycleEvent");
    moveToState(event.getTargetState());
}
```

转移到正确的生命周期状态。
```java
private void moveToState(State next) {
    //注释1处，新的状态和当前状态一样，直接return
    if (mState == next) {
        return;
    }
    //为当前状态赋值
    mState = next;
    //注释2处
    if (mHandlingEvent || mAddingObserverCounter != 0) {
        mNewEventOccurred = true;
        // we will figure out what to do on upper level.
        return;
    }
    //将mHandlingEvent置为true，表示正在处理事件，然后调用sync()方法
    mHandlingEvent = true;
    sync();
    mHandlingEvent = false;
}
```
moveToState()方法的注释1处，新的状态和当前状态一样，直接return。

注释2处，如果条件满足就将mNewEventOccurred置为true，表示有新的事件产生，然后返回。
```java
if (mHandlingEvent || mAddingObserverCounter != 0) {
    //有新的事件产生
    mNewEventOccurred = true;
    return;
}
```
`mHandlingEvent`表示是否正在处理事件，`mAddingObserverCounter`在标志是否正在添加观察者，如果正在添加观察者`mAddingObserverCounter>0`，否则`mAddingObserverCounter=0`。

这个条件判断其实是为了保证sync()方法不会被重入调用，即保证当调用sync()方法的时候，上一次sync()方法调用必须已经执行完毕。也就是说保证不存在sync()刚执行到一半，然后sync()又被调用的情况。

接下来我们看一下sync方法。

```java
private void sync() {
    LifecycleOwner lifecycleOwner = mLifecycleOwner.get();
    //...
    //注释1处
    while (!isSynced()) {
        mNewEventOccurred = false;
        // 注释2处
        if (mState.compareTo(mObserverMap.eldest().getValue().mState) < 0) {
            backwardPass(lifecycleOwner);
        }
        
        Entry<LifecycleObserver, ObserverWithState> newest = mObserverMap.newest();
        if (!mNewEventOccurred && newest != null
                && mState.compareTo(newest.getValue().mState) > 0) {
            //注释3处
            forwardPass(lifecycleOwner);
        }
    }
    mNewEventOccurred = false;
}
```

注释1处，判断生命周期状态是否已经同步，如果已经同步过了，则直接返回不需要通知观察者。
```
private boolean isSynced() {
    //如果不存在观察者返回true
    if (mObserverMap.size() == 0) {
        return true;
    }
    //最老的观察者状态
    State eldestObserverState = mObserverMap.eldest().getValue().mState;
    //最新的观察者状态
    State newestObserverState = mObserverMap.newest().getValue().mState;
    //根据新旧状态和当前状态判断
    return eldestObserverState == newestObserverState && mState == newestObserverState;
}
```

注释2处，生命周期状态值大小顺序从大到小
```
RESUMED > STARTED > CREATED > INITIALIZED > DESTROYED
```

当前状态小于最老的观察者的状态，什么意思呢？比如说，当前状态是STARTED而最老的观察者的状态是RESUMED，STARTED<RESUMED，也就是说LifecycleOwner从onResume状态即将切换到onPause状态。

```java
private void backwardPass(LifecycleOwner lifecycleOwner) {
    Iterator<Entry<LifecycleObserver, ObserverWithState>> descendingIterator =
            mObserverMap.descendingIterator();
    while (descendingIterator.hasNext() && !mNewEventOccurred) {
        Entry<LifecycleObserver, ObserverWithState> entry = descendingIterator.next();
        ObserverWithState observer = entry.getValue();
        //注释1处
        while ((observer.mState.compareTo(mState) > 0 && !mNewEventOccurred
                && mObserverMap.contains(entry.getKey()))) {
            //减小状态
            Event event = downEvent(observer.mState);
            pushParentState(getStateAfter(event));
            observer.dispatchEvent(lifecycleOwner, event);
            popParentState();
        }
    }
}
```
该方法循环遍历要通知的观察者，然后减小观察者的状态，直到观察者的状态和当前状态`mState`一样。

sync方法的注释3处，如果当前状态大于最新观察者的状态，则增大观察者的状态。

```java
private void forwardPass(LifecycleOwner lifecycleOwner) {
    Iterator<Entry<LifecycleObserver, ObserverWithState>> ascendingIterator =
            mObserverMap.iteratorWithAdditions();
    while (ascendingIterator.hasNext() && !mNewEventOccurred) {
        Entry<LifecycleObserver, ObserverWithState> entry = ascendingIterator.next();
        ObserverWithState observer = entry.getValue();
        while ((observer.mState.compareTo(mState) < 0 && !mNewEventOccurred
                && mObserverMap.contains(entry.getKey()))) {
            pushParentState(observer.mState);
            observer.dispatchEvent(lifecycleOwner, upEvent(observer.mState));
            popParentState();
        }
    }
}
```


我们再看一下LifecycleRegistry的addObserver()方法
```java
@Override
public void addObserver(@NonNull LifecycleObserver observer) {
    //正常情况下，默认状态是INITIALIZED
    State initialState = mState == DESTROYED ? DESTROYED : INITIALIZED;
    //将传入的LifecycleObserver包装成一个ObserverWithState对象。
    ObserverWithState statefulObserver = new ObserverWithState(observer, initialState);
    //只添加一次，避免重复添加
    ObserverWithState previous = mObserverMap.putIfAbsent(observer, statefulObserver);

    //注释1处，previous != null表示重复添加，直接返回
    if (previous != null) {
        return;
    }
    LifecycleOwner lifecycleOwner = mLifecycleOwner.get();
    if (lifecycleOwner == null) {
        // it is null we should be destroyed. Fallback quickly
        return;
    }

    //是否是重入调用
    boolean isReentrance = mAddingObserverCounter != 0 || mHandlingEvent;
    //注释2处，计算observer的目标状态
    State targetState = calculateTargetState(observer);
    //将mAddingObserverCounter加1，表示正在添加观察者
    mAddingObserverCounter++;
    //注释2处
    while ((statefulObserver.mState.compareTo(targetState) < 0
            && mObserverMap.contains(observer))) {
        pushParentState(statefulObserver.mState);
        //注释3处
        statefulObserver.dispatchEvent(lifecycleOwner, upEvent(statefulObserver.mState));
        popParentState();
        // mState / sibling 可能发生了改变，重新计算targetState
        targetState = calculateTargetState(observer);
    }
    //注释4处，保证sync方法的非重入调用
    if (!isReentrance) {
        sync();
    }
    //添加完，将mAddingObserverCounter减1
    mAddingObserverCounter--;
}
```
注释1处，避免重复添加观察者。

注释2处，计算新添加的观察者的目标状态。
```java
private State calculateTargetState(LifecycleObserver observer) {
    Entry<LifecycleObserver, ObserverWithState> previous = mObserverMap.ceil(observer);
    //如果存在已经添加的观察者，获取其状态
    State siblingState = previous != null ? previous.getValue().mState : null;
    State parentState = !mParentStates.isEmpty() ? mParentStates.get(mParentStates.size() - 1)
                : null;
    //最终返回最小的状态
    return min(min(mState, siblingState), parentState);
}
```
正常情况下，不用关注parentState这个变量，最终返回最小的状态。

注释2处，如果观察者的状态还没到达目标状态，则根据观察者的当前状态计算出生命周期事件，直到观察者到达目标状态。

举个例子：比如我们等Activity onResume以后，我们点击按钮添加一个观察者，那么这个观察者的状态会从INITIALIZED -> CREATED -> STARTED -> RESUMED。

注释4处，判断是否是重入，什么意思呢，就是为了保证sync方法不会被重入调用，即保证当调用sync方法的时候，上一次sync方法调用必须已经执行完毕。也就是说保证不存在sync刚执行到一半，然后sync又被调用的情况。

参考链接：

* [使用生命周期感知型组件处理生命周期](https://developer.android.google.cn/topic/libraries/architecture/lifecycle?hl=zh_cn)















