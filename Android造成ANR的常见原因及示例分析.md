## 什么是ANR

在Android中，如果主线程被长时间阻塞，导致无法响应用户的操作，即造成ANR（Application Not Responding）。通常的表现是弹出一个应用无响应的对话框，让用户选择强制退出或者等待。

![ANR_Dialog.png](https://upload-images.jianshu.io/upload_images/3611193-288e9c678bcfd645.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



注意：主线程做耗时操作本身是不会产生ANR的，导致ANR的根本还是应用程序无法在一定时间内响应用户的操作。因为主线程被耗时操作阻塞了，主线程无法对下一个操作进行响应才会ANR，没有需要响应的操作自然就不会产生ANR，或者应该这样说：主线程做耗时操作，非常容易引发ANR。

## ANR的类型

KeyDispatch Timeout ：按键或触摸事件在特定时间内无响应。超时时间5秒。超时时间是在`ActivityManagerService`类中定义的。

```java
// How long we wait until we timeout on key dispatching.
static final int KEY_DISPATCHING_TIMEOUT = 5*1000;
```

Broadcast Timeout ：BroadcastReceiver在特定时间内无法处理完成。前台广播10秒，后台广播60秒。超时时间是在`ActivityManagerService`类中定义的。

```java
// How long we allow a receiver to run before giving up on it.
static final int BROADCAST_FG_TIMEOUT = 10*1000;
static final int BROADCAST_BG_TIMEOUT = 60*1000;
```

Service Timeout ：Service在特定的时间内生命周期函数无法处理完成。前台服务20秒，后台服务200秒。超时时间是在`ActiveServices`类中定义的。

```java
// How long we wait for a service to finish executing.
static final int SERVICE_TIMEOUT = 20*1000;
// How long we wait for a service to finish executing.
static final int SERVICE_BACKGROUND_TIMEOUT = SERVICE_TIMEOUT * 10;
```

ContentProvider Timeout ：ContentProvider在特定的时间内没有完成发布。超时时间10秒。超时时间是在`ActivityManagerService`类中定义的。

```java
// How long we wait for an attached process to publish its content providers
// before we decide it must be hung.
static final int CONTENT_PROVIDER_PUBLISH_TIMEOUT = 10*1000;
```

关于ANR类型的详细信息不在本篇文章的叙述范围之内，请自行查阅资料。


## 造成ANR的常见原因

1. 应用在主线程上进行长时间的计算。
2. 应用在主线程上执行耗时的I/O的操作。
3. 主线程处于阻塞状态，等待获取锁。
4. 主线程与其他线程之间发生死锁。
5. 主线程在对另一个进程进行同步Binder调用，而后者需要很长时间才能返回。(如果我们知道调用远程方法需要很长时间，我们应该避免在主线程调用)

上述原因都会造成主线程被长时间阻塞，导致无法响应用户的操作，从而造成ANR。

## ANR原因排查

ANR发生以后，在Logcat中有相应的日志输出，并且会在`/data/anr/`目录下输出一个`traces.tx`文件，该文件记录了ANR的更加详细的信息，我们可以导出分析。接下来我们就依次模拟上述5种方式来制造ANR，然后分析产生的Logcat和traces.txt文件。

测试环境：Android Studio 3.6.1  
测试手机： HUAWEI MLA-AL10，Android版本: 7.0


### 1.应用在主线程上进行的计算

```kotlin
//使用冒泡排序对一个大数组排序
private fun sortBigArray() {
    val currTime = System.currentTimeMillis()
    val random = IntArray(1000000)
    for (i in random.indices) {
        random[i] = (Math.random() * 10000000).toInt()
    }
    BubbleSort.sort(random)
    println("耗时" + (System.currentTimeMillis() - currTime) + "ms")
    for (i in random.indices) {
        println(random[i].toString())
    }
}
```
我们点击一个按钮调用sortBigArray()方法，内部调用BubbleSort类的sort()方法对一个大数组（100万）进行排序，然后点击几次返回键，然后就出现ANR了。


**我们先看一下Logcat日志输出**

```
//debug级别日志
2020-06-03 21:20:24.209 com.example.android.jetpackdemo I/art: Wrote stack traces to '/data/anr/traces.txt'

//error级别日志
2020-06-03 21:20:28.048 ? E/ActivityManager: ANR in com.example.android.jetpackdemo (com.example.android.jetpackdemo/.StartActivity)
    PID: 15564
    Reason: Input dispatching timed out (Waiting to send key event because the focused window has not finished processing all of the input events that were previously delivered to it.  Outbound queue length: 0.  Wait queue length: 2.)
    Load: 7.7 / 7.48 / 7.35
    CPU usage from 294322ms to 0ms ago (2020-06-03 21:15:29.817 to 2020-06-03 21:20:24.139):
      4.1% 2001/system_server: 3.1% user + 0.9% kernel / faults: 64102 minor 6 major
      3.3% 29428/adbd: 0.8% user + 2.4% kernel / faults: 131259 minor
      1.1% 508/logd: 0.5% user + 0.6% kernel / faults: 18 minor
      0.7% 2661/com.android.systemui: 0.6% user + 0.1% kernel / faults: 1648 minor 1 major
      0.7% 607/surfaceflinger: 0.4% user + 0.3% kernel / faults: 21 minor
      0.7% 24463/com.huawei.hwid.persistent: 0.6% user + 0% kernel / faults: 4650 minor 1 major
      0.5% 4018/com.huawei.android.launcher: 0.4% user + 0% kernel / faults: 16025 minor 3 major
      0.5% 24301/fingerprint_log: 0% user + 0.5% kernel
      0.4% 28932/com.huawei.appmarket: 0.3% user + 0% kernel / faults: 2526 minor
     //...
   2020-06-03 21:20:28.048 ? E/ActivityManager: CPU usage from 1721ms to 2250ms later (2020-06-03 21:20:25.860 to 2020-06-03 21:20:26.389):
      99% 15564/com.example.android.jetpackdemo: 97% user + 1.8% kernel / faults: 37 minor
        99% 15564/oid.jetpackdemo: 99% user + 0% kernel
      7.5% 2001/system_server: 3.7% user + 3.7% kernel / faults: 5 minor
        5.6% 2014/ActivityManager: 1.8% user + 3.7% kernel
        1.8% 2813/Binder:2001_5: 1.8% user + 0% kernel
        1.8% 2862/Binder:2001_6: 0% user + 1.8% kernel
        1.8% 3089/Binder:2001_7: 1.8% user + 0% kernel
      5.3% 29428/adbd: 0% user + 5.3% kernel / faults: 480 minor
        3.5% 29430/->transport: 0% user + 3.5% kernel
        1.7% 29428/adbd: 0% user + 1.7% kernel
      1.3% 53/rcuop/6: 0% user + 1.3% kernel
    16% TOTAL: 14% user + 2.1% kernel + 0.2% irq + 0.2% softirq

```

在上面的日志中输出了堆栈信息的保存在 `/data/anr/traces.txt`文件中。

```
com.example.android.jetpackdemo I/dalvikvm: Wrote stack traces to '/data/anr/traces.txt'
```

发生ANR进程的包名信息，所在的类，进程id和ANR的类型

```
2020-06-03 21:20:28.048 ? E/ActivityManager: ANR in com.example.android.jetpackdemo (com.example.android.jetpackdemo/.StartActivity)
    PID: 15564
    Reason: Input dispatching timed out (Waiting to send key event because the focused window has not finished processing all of the input events that were previously delivered to it.  Outbound queue length: 0.  Wait queue length: 2.)  
```
包名`com.example.android.jetpackdemo`，具体的类`com.example.android.jetpackdemo.StartActivity`，进程号是`PID: 15564`，ANR的类型是`Input dispatching timed out`。

```
 CPU usage from 294322ms to 0ms ago (2020-06-03 21:15:29.817 to 2020-06-03 21:20:24.139):
      4.1% 2001/system_server: 3.1% user + 0.9% kernel / faults: 64102 minor 6 major
      3.3% 29428/adbd: 0.8% user + 2.4% kernel / faults: 131259 minor
      1.1% 508/logd: 0.5% user + 0.6% kernel / faults: 18 minor

//...

2020-06-03 21:20:28.048 ? E/ActivityManager: CPU usage from 1721ms to 2250ms later (2020-06-03 21:20:25.860 to 2020-06-03 21:20:26.389):
      99% 15564/com.example.android.jetpackdemo: 97% user + 1.8% kernel / faults: 37 minor
        99% 15564/oid.jetpackdemo: 99% user + 0% kernel
      7.5% 2001/system_server: 3.7% user + 3.7% kernel / faults: 5 minor
        5.6% 2014/ActivityManager: 1.8% user + 3.7% kernel
        1.8% 2813/Binder:2001_5: 1.8% user + 0% kernel

//...
```

这两段CPU 信息分别代表ANR发生前和ANR时的CPU占用率，在输出的CPU使用信息中我们也可以看出一些端倪，我们注意到我们的进程CPU的占用率比较高，说明我们的进程比较忙碌，这里需要说明一下，进程忙碌并不一定代表主线程忙碌，也可能是进程中的后台线程忙碌。

但是现在我们虽然知道了ANR发生的所在的类，但是如何精确定位到具体的哪一行代码呢？这就需要分析发生ANR的时候保存的traces.txt文件了。

**导出traces文件**

使用adb命令导出traces.txt文件

```
adb pull /data/anr/traces.txt traces_1.txt 
/data/anr/traces.txt: 1 file pulled, 0 skipped. 28.5 MB/s (701726 bytes in 0.023s)
```

如果入到permission相关问题，请使用bugreport命令导出，参考 [Capture and read bug reports](https://developer.android.com/studio/debug/bug-report)。

traces.txt部分信息

```
----- pid 15564 at 2020-06-03 21:20:24 -----
Cmd line: com.example.android.jetpackdemo
Build fingerprint: 'HUAWEI/MLA-AL10/HWMLA:7.0/HUAWEIMLA-AL10/C00B364:user/release-keys'
ABI: 'arm64'
//...

```
在traces.txt文件的最顶部，首先输出的是发生ANR的进程号和包名信息，然后我们可以在traces.txt中搜索我们的进程号或者包名。


```
"main" prio=5 tid=1 Runnable
  | group="main" sCount=0 dsCount=0 obj=0x77d21af8 self=0x7fa2ea2a00
  | sysTid=15564 nice=-10 cgrp=default sched=0/0 handle=0x7fa6f4ba98
  | state=R schedstat=( 22116939220 18299419 428 ) utm=2209 stm=2 core=5 HZ=100
  | stack=0x7fd42e0000-0x7fd42e2000 stackSize=8MB
  | held mutexes= "mutator lock"(shared held)
  at com.example.android.jetpackdemo.BubbleSort.sort(BubbleSort.java:45)
  at com.example.android.jetpackdemo.StartActivity.sortBigArray(StartActivity.kt:76)
  at com.example.android.jetpackdemo.StartActivity.onClick(StartActivity.kt:47)
  at java.lang.reflect.Method.invoke!(Native method)
  at androidx.appcompat.app.AppCompatViewInflater$DeclaredOnClickListener.onClick(AppCompatViewInflater.java:397)
  at android.view.View.performClick(View.java:5646)
  at android.view.View$PerformClick.run(View.java:22473)
  at android.os.Handler.handleCallback(Handler.java:761)
  at android.os.Handler.dispatchMessage(Handler.java:98)
  at android.os.Looper.loop(Looper.java:156)
  at android.app.ActivityThread.main(ActivityThread.java:6517)
  at java.lang.reflect.Method.invoke!(Native method)
  at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:942)
  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:832)
//...
```

我们首先看一下和线程相关的部分信息。

```
"main" prio=5 tid=1 Runnable
  | group="main" sCount=0 dsCount=0 obj=0x77d21af8 self=0x7fa2ea2a00
  | sysTid=15564 nice=-10 cgrp=default sched=0/0 handle=0x7fa6f4ba98
  | state=R schedstat=( 22116939220 18299419 428 ) utm=2209 stm=2 core=5 HZ=100
  | stack=0x7fd42e0000-0x7fd42e2000 stackSize=8MB
  | held mutexes= "mutator lock"(shared held)
```

**线程基本信息：**

* 线程名：main

* 线程优先级：prio=5，优先级取值范围[1,10]，详见`Thread`类：

```
//最小取值
public final static int MIN_PRIORITY = 1;
//默认优先级
public final static int NORM_PRIORITY = 5;
//最大取值
public final static int MAX_PRIORITY = 10;
```

* 线程id： tid=1，1代表主线程
* 线程状态：Runnable，状态取值如下，详见`Thread.State`枚举类：

```java
NEW, //线程还没启动

RUNNABLE, //正在执行

BLOCKED, //等待获取锁

WAITING, //等待其他线程执行一个特定的动作，比如说调用Object.notify或Object.notifyAll()

TIMED_WAITING, //等待一定时间

TERMINATED //执行完毕
```

* 线程组名称：group="main"
* 线程被挂起的次数：sCount=0
* 线程被调试器挂起的次数：dsCount=0
* 线程的java的对象地址：obj= 0x77d21af8
* 线程本身的Native对象地址：self= 0x7fa2ea2a00

**线程调度信息：**

* Linux系统中内核线程id: sysTid= 15564 与进程号相同
* 线程调度优先级：nice=-10，详细信息可参考 [浅析Linux线程调度](https://www.cnblogs.com/wanghuaijun/p/7954029.html)
* 线程调度组：cgrp=default
* 线程调度策略和优先级：sched=0/0
* 线程处理函数地址：handle= 0x7fa6f4ba98

**线程的堆栈信息：**

* 堆栈地址和大小：stack=0x7fd42e0000-0x7fd42e2000 stackSize=8MB


**held mutexes：** 

* held mutexes 到底是什么意思我没有找到官方的文档解释，网上大多数关于held mutexes的解释也都是一笔带过没有实际参考意义，我们这里先忽略这个东西，并不会影响我们排查问题。


从上面traces.txt文件中这段信息可以看出，导致ANR的最终原因是在BubbleSort.java的第45行。

```
 at com.example.android.jetpackdemo.BubbleSort.sort(BubbleSort.java:45)
 at com.example.android.jetpackdemo.StartActivity.sortBigArray(StartActivity.kt:76)
 at com.example.android.jetpackdemo.StartActivity.onClick(StartActivity.kt:47)
 at java.lang.reflect.Method.invoke!(Native method)
```

![anr_1.png](https://upload-images.jianshu.io/upload_images/3611193-a660fdd3224178a2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)


### 2.应用在主线程上执行耗时的I/O的操作


```kotlin
/**
 * 拷贝文件，注意要有读写权限
 */
private fun doIo() {
    val prePath = Environment.getExternalStorageDirectory().path
    val file = File("${prePath}/test/View.java")
    if (file.exists()) {
        Log.d(TAG, "doIo: ${file.length()}")

        val reader = FileReader(file)
        val fileWriter = FileWriter("${prePath}/test/ViewCopy.java", true)

        for (index in 0 until 5) {
            var count: Int
            while (reader.read().also { count = it } != -1) {
                fileWriter.write(count)
            }
            try {
                reader.reset()
            } catch (e: IOException) {
                Log.d(TAG, "doIo: error ${e.message}")
            }
        }
    }
}
```

调用doIo()方法以后，多次点击返回键，制造ANR。
    
Logcat日志输出

```
2020-06-04 21:05:24.462 ? E/ActivityManager: ANR in com.example.android.jetpackdemo (com.example.android.jetpackdemo/.StartActivity)
    PID: 16295
    Reason: Input dispatching timed out (Waiting to send key event because the focused window has not finished processing all of the input events that were previously delivered to it.  Outbound queue length: 0.  Wait queue length: 2.)
    Load: 7.49 / 7.45 / 7.24
    CPU usage from 87491ms to 0ms ago (2020-06-04 21:03:53.035 to 2020-06-04 21:05:20.526):
      7.8% 2001/system_server: 6.1% user + 1.7% kernel / faults: 34095 minor 3 major
      4.2% 28932/com.huawei.appmarket: 3.7% user + 0.5% kernel / faults: 12314 minor 5 major
      2.8% 2661/com.android.systemui: 2.2% user + 0.5% kernel / faults: 4222 minor 1 major
      2% 412/msm-core:sampli: 0% user + 2% kernel
      1.7% 24463/com.huawei.hwid.persistent: 1.5% user + 0.1% kernel / faults: 3317 minor 1 major
      1.5% 607/surfaceflinger: 1% user + 0.5% kernel / faults: 24 minor
     //...    
2020-06-04 21:05:24.462 ? E/ActivityManager: CPU usage from 1696ms to 2226ms later (2020-06-04 21:05:22.222 to 2020-06-04 21:05:22.752):
      84% 16295/com.example.android.jetpackdemo: 84% user + 0% kernel / faults: 562 minor 1 major
        68% 16295/oid.jetpackdemo: 68% user + 0% kernel
        12% 16317/RenderThread: 12% user + 0% kernel
        1.8% 16307/HeapTaskDaemon: 1.8% user + 0% kernel
       +0% 16461/DeferredSaveThr: 0% user + 0% kernel
      9.1% 2001/system_server: 1.8% user + 7.3% kernel / faults: 7 minor
        7.3% 2014/ActivityManager: 0% user + 7.3% kernel
        3.6% 2536/Binder:2001_3: 3.6% user + 0% kernel
      5.5% 607/surfaceflinger: 2.7% user + 2.7% kernel
        1.3% 607/surfaceflinger: 1.3% user + 0% kernel
        1.3% 658/Binder:607_1: 0% user + 1.3% kernel
      4.3% 2661/com.android.systemui: 2.9% user + 1.4% kernel / faults: 26 minor
        4.3% 3614/RenderThread: 2.9% user + 1.4% kernel
        1.4% 2661/ndroid.systemui: 1.4% user + 0% kernel
      1.3% 25/rcuop/2: 0% user + 1.3% kernel
      1.3% 339/irq/171-tsens_i: 0% user + 1.3% kernel
      1.5% 11851/mdss_fb0: 0% user + 1.5% kernel
      1.6% 14246/kworker/u16:5: 0% user + 1.6% kernel
      1.6% 16318/kworker/u16:4: 0% user + 1.6% kernel
    15% TOTAL: 13% user + 1.8% kernel
```

从上面的日志信息中我们也看出来发生ANR的时候，我们的进程`com.example.android.jetpackdemo`CPU占用率是比较高的，说明我们进程内存在比较忙碌的线程。然后我们继续看一下对应的traces.txt文件。

**traces.txt部分信息**

```
----- pid 16295 at 2020-06-04 21:05:20 -----
Cmd line: com.example.android.jetpackdemo
Build fingerprint: 'HUAWEI/MLA-AL10/HWMLA:7.0/HUAWEIMLA-AL10/C00B364:user/release-keys'
```

通过进程号pid 16295搜索

```
"main" prio=5 tid=1 Runnable
  | group="main" sCount=0 dsCount=0 obj=0x77d21af8 self=0x7fa2ea2a00
  | sysTid=16295 nice=-10 cgrp=default sched=0/0 handle=0x7fa6f4ba98
  | state=R schedstat=( 16406184130 12254163 407 ) utm=1630 stm=10 core=6 HZ=100
  | stack=0x7fd42e0000-0x7fd42e2000 stackSize=8MB
  | held mutexes= "mutator lock"(shared held)
  native: #00 pc 0000000000478088  /system/lib64/libart.so (_ZN3art15DumpNativeStackERNSt3__113basic_ostreamIcNS0_11char_traitsIcEEEEiP12BacktraceMapPKcPNS_9ArtMethodEPv+220)
  native: #01 pc 0000000000478084  /system/lib64/libart.so (_ZN3art15DumpNativeStackERNSt3__113basic_ostreamIcNS0_11char_traitsIcEEEEiP12BacktraceMapPKcPNS_9ArtMethodEPv+216)
  native: #02 pc 000000000044c604  /system/lib64/libart.so (_ZNK3art6Thread9DumpStackERNSt3__113basic_ostreamIcNS1_11char_traitsIcEEEEbP12BacktraceMap+524)
  native: #03 pc 0000000000463f60  /system/lib64/libart.so (_ZN3art14DumpCheckpoint3RunEPNS_6ThreadE+820)
  native: #04 pc 000000000044d510  /system/lib64/libart.so (_ZN3art6Thread21RunCheckpointFunctionEv+192)
  native: #05 pc 00000000000ff870  /system/lib64/libart.so (_ZN3art27ScopedObjectAccessUncheckedD2Ev+576)
  native: #06 pc 000000000010a764  /system/lib64/libart.so (_ZN3art8CheckJNI23GetPrimitiveArrayRegionEPKcNS_9Primitive4TypeEP7_JNIEnvP7_jarrayiiPv+1164)
  native: #07 pc 0000000000022ee4  /system/lib64/libjavacore.so (???)
  native: #08 pc 00000000004747a8  /data/dalvik-cache/arm64/system@framework@boot-core-libart.oat (Java_libcore_icu_NativeConverter_encode__J_3CI_3BI_3IZ+244)
  at libcore.icu.NativeConverter.encode(Native method)
  at java.nio.charset.CharsetEncoderICU.encodeLoop(CharsetEncoderICU.java:169)
  at java.nio.charset.CharsetEncoder.encode(CharsetEncoder.java:579)
  at sun.nio.cs.StreamEncoder.implWrite(StreamEncoder.java:271)
  at sun.nio.cs.StreamEncoder.write(StreamEncoder.java:125)
  - locked <0x05b5279d> (a java.io.FileWriter)
  at sun.nio.cs.StreamEncoder.write(StreamEncoder.java:113)
  at java.io.OutputStreamWriter.write(OutputStreamWriter.java:194)
  at com.example.android.jetpackdemo.StartActivity.doIo(StartActivity.kt:116)
  at com.example.android.jetpackdemo.StartActivity.onClick(StartActivity.kt:65)
  at java.lang.reflect.Method.invoke!(Native method)
  at androidx.appcompat.app.AppCompatViewInflater$DeclaredOnClickListener.onClick(AppCompatViewInflater.java:397)
  at android.view.View.performClick(View.java:5646)
  at android.view.View$PerformClick.run(View.java:22473)
  at android.os.Handler.handleCallback(Handler.java:761)
  at android.os.Handler.dispatchMessage(Handler.java:98)
  at android.os.Looper.loop(Looper.java:156)
  at android.app.ActivityThread.main(ActivityThread.java:6517)
  at java.lang.reflect.Method.invoke!(Native method)
  at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:942)
  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:832)
```

我们重点看一下这段信息

```
at java.io.OutputStreamWriter.write(OutputStreamWriter.java:194)
at com.example.android.jetpackdemo.StartActivity.doIo(StartActivity.kt:116)
at com.example.android.jetpackdemo.StartActivity.onClick(StartActivity.kt:65)
```

从上面这段信息可以看出，导致ANR的最终原因是在OutputStreamWriter.java的第194行。而我们的代码出问题的地方是StartActivity.kt的116行。

![OutputStreamWriter_194.png](https://upload-images.jianshu.io/upload_images/3611193-8ff82e7a70865623.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)
![StartActivity_116.png](https://upload-images.jianshu.io/upload_images/3611193-f14c3e0a30f5567f.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 3.主线程处于阻塞状态，等待获取锁

```
//锁资源
val lockedResource: Any = Any()

fun onClick(v: View) {
    when (v.id) {
        R.id.btnWaitLockedResource -> {
            LockTask().execute(arrayListOf<Int>())
            Log.d(TAG, "onClick: 主线程先睡眠一会，避免先获取到锁")
            Thread.sleep(200)
            Log.d(TAG, "onClick: 主线程先睡眠结束，尝试获取锁")
            synchronized(lockedResource) {
                for (index in 0 until 10) {
                    Log.d(TAG, "onClick: 主线程获取到锁了$index")
                }
            }
        }
    }
}


//LockTask后台线程
inner class LockTask : AsyncTask<MutableList<Int>, Int, Unit>() {
    override fun doInBackground(vararg params: MutableList<Int>) =
        synchronized(lockedResource) {
            val list = params[0]
            for (i in 0 until 1000000) {
                list.add((Math.random() * 10000000).toInt())
            }
            list.forEach {
                Log.d(TAG, "doInBackground: for each element is $it")
            }
        }
}

```

调用onClick()方法以后，先让后台线程获取锁，然后主线程再尝试获取锁。然后多次点击返回键，制造ANR。


**Logcat日志输出**

```
2020-06-04 09:55:04.396 ? E/ActivityManager: ANR in com.example.android.jetpackdemo (com.example.android.jetpackdemo/.StartActivity)
    PID: 20008
    Reason: Input dispatching timed out (Waiting to send key event because the focused window has not finished processing all of the input events that were previously delivered to it.  Outbound queue length: 0.  Wait queue length: 2.)
    Load: 8.27 / 7.73 / 7.37
    CPU usage from 83152ms to 0ms ago (2020-06-04 09:53:36.842 to 2020-06-04 09:54:59.995) with 99% awake:
      19% 508/logd: 15% user + 3.5% kernel / faults: 533 minor 1 major
      5.5% 2001/system_server: 3.9% user + 1.5% kernel / faults: 10843 minor 7 major
      4.9% 28932/com.huawei.appmarket: 4.3% user + 0.6% kernel / faults: 13003 minor 79 major
      2.6% 2661/com.android.systemui: 2.2% user + 0.3% kernel / faults: 7158 minor 2 major
      1.5% 607/surfaceflinger: 0.9% user + 0.6% kernel / faults: 190 minor 1 major
      1.2% 24307/logcat: 0.7% user + 0.4% kernel
      0.8% 11161/com.android.settings: 0.6% user + 0.1% kernel / faults: 9084 minor 20 major
      0.6% 24305/logcat: 0.2% user + 0.3% kernel
      0.4% 24301/fingerprint_log: 0% user + 0.4% kernel
      0.3% 15363/kworker/u16:10: 0% user + 0.3% kernel
      0.2% 6831/kworker/u16:5: 0% user + 0.2% kernel
      0.2% 837/imonitor: 0% user + 0.1% kernel
      //...
     2020-06-04 09:55:04.396 ? E/ActivityManager: CPU usage from 2211ms to 2742ms later (2020-06-04 09:55:02.206 to 2020-06-04 09:55:02.737):
      105% 20008/com.example.android.jetpackdemo: 92% user + 13% kernel / faults: 220 minor
        99% 20096/AsyncTask #1: 86% user + 13% kernel
        5.6% 20019/HeapTaskDaemon: 5.6% user + 0% kernel
      103% 508/logd: 99% user + 3.7% kernel / faults: 8 minor
        92% 24315/logd.reader.per: 92% user + 0% kernel
        7.5% 511/logd.writer: 5.6% user + 1.8% kernel
        3.7% 24314/logd.reader.per: 0% user + 3.7% kernel
        1.8% 24313/logd.reader.per: 0% user + 1.8% kernel
      11% 2661/com.android.systemui: 11% user + 0% kernel / faults: 52 minor
        9.3% 3614/RenderThread: 7.5% user + 1.8% kernel
        1.8% 2661/ndroid.systemui: 1.8% user + 0% kernel
      9.3% 607/surfaceflinger: 9.3% user + 0% kernel
        3.7% 607/surfaceflinger: 3.7% user + 0% kernel
        1.8% 2614/Binder:607_4: 0% user + 1.8% kernel
      5.6% 2001/system_server: 1.8% user + 3.7% kernel / faults: 2 minor
        5.6% 2014/ActivityManager: 0% user + 5.6% kernel
      3.3% 19794/adbd: 1.6% user + 1.6% kernel / faults: 147 minor
        1.6% 19794/adbd: 0% user + 1.6% kernel
        1.6% 19796/->transport: 0% user + 1.6% kernel
        1.6% 19797/<-transport: 0% user + 1.6% kernel
      3.4% 24307/logcat: 0% user + 3.4% kernel
      1.3% 624/mm-pp-dpps: 0% user + 1.3% kernel
        1.3% 717/ABA_THREAD: 1.3% user + 0% kernel
      1.6% 18971/kworker/0:2: 0% user + 1.6% kernel
      1.6% 18974/kworker/u16:0: 0% user + 1.6% kernel
      1.6% 19095/mdss_fb0: 0% user + 1.6% kernel
      1.7% 24301/fingerprint_log: 1.7% user + 0% kernel
      1.7% 24305/logcat: 1.7% user + 0% kernel
    31% TOTAL: 26% user + 4% kernel + 0.2% irq + 0.2% softirq

```

从上面的日志信息中我们也看出来我们的进程CPU占用率是比较高的，说明我们进程内存在比较忙碌的线程。然后我们继续看一下对应的traces.txt文件。

traces.txt部分信息

```
----- pid 20008 at 2020-06-04 09:55:00 -----
Cmd line: com.example.android.jetpackdemo
Build fingerprint: 'HUAWEI/MLA-AL10/HWMLA:7.0/HUAWEIMLA-AL10/C00B364:user/release-keys'
```

通过进程号pid 20008搜索

```
"main" prio=5 tid=1 Blocked
  | group="main" sCount=1 dsCount=0 obj=0x77d21af8 self=0x7fa2ea2a00
  | sysTid=20008 nice=-10 cgrp=default sched=0/0 handle=0x7fa6f4ba98
  | state=S schedstat=( 278831875 7233747 156 ) utm=22 stm=5 core=0 HZ=100
  | stack=0x7fd42e0000-0x7fd42e2000 stackSize=8MB
  | held mutexes=
  at com.example.android.jetpackdemo.StartActivity.onClick(StartActivity.kt:61)
  - waiting to lock <0x0f8c80b0> (a java.lang.Object) held by thread 16
  at java.lang.reflect.Method.invoke!(Native method)
  at androidx.appcompat.app.AppCompatViewInflater$DeclaredOnClickListener.onClick(AppCompatViewInflater.java:397)
  at android.view.View.performClick(View.java:5646)
  at android.view.View$PerformClick.run(View.java:22473)
  at android.os.Handler.handleCallback(Handler.java:761)
  at android.os.Handler.dispatchMessage(Handler.java:98)
  at android.os.Looper.loop(Looper.java:156)
  at android.app.ActivityThread.main(ActivityThread.java:6517)
  at java.lang.reflect.Method.invoke!(Native method)
  at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:942)
  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:832)
```

关键信息

```
at com.example.android.jetpackdemo.StartActivity.onClick(StartActivity.kt:61)
- waiting to lock <0x0f8c80b0> (a java.lang.Object) held by thread 16
```

在StartActivity的61行，在等待一个锁对象`<0x0f8c80b0>`，该对象是一个`Object`对象(a java.lang.Object)，这个锁对象正在被线程id为16的线程持有。那么我们下面在traces.txt文件中搜索一下这个锁对象`<0x0f8c80b0>`。如下所示：

```
DALVIK THREADS (16):
"AsyncTask #1" prio=5 tid=16 Runnable
  | group="main" sCount=0 dsCount=0 obj=0x12cd61f0 self=0x7f93187200
  | sysTid=20096 nice=10 cgrp=bg_non_interactive sched=0/0 handle=0x7f84346450
  | state=R schedstat=( 13814173056 6030204 1355 ) utm=1193 stm=188 core=3 HZ=100
  | stack=0x7f84244000-0x7f84246000 stackSize=1037KB
  | held mutexes= "mutator lock"(shared held)
  at java.lang.Integer.stringSize(Integer.java:414)
  at java.lang.AbstractStringBuilder.append(AbstractStringBuilder.java:630)
  at java.lang.StringBuilder.append(StringBuilder.java:220)
  at com.example.android.jetpackdemo.StartActivity$LockTask.doInBackground(StartActivity.kt:107)
  - locked <0x0f8c80b0> (a java.lang.Object)
  at com.example.android.jetpackdemo.StartActivity$LockTask.doInBackground(StartActivity.kt:99)
  at android.os.AsyncTask$2.call(AsyncTask.java:316)
  at java.util.concurrent.FutureTask.run(FutureTask.java:237)
  at android.os.AsyncTask$SerialExecutor$1.run(AsyncTask.java:255)
  at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1133)
  at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:607)
  at java.lang.Thread.run(Thread.java:776)
```

关键信息

```
 at com.example.android.jetpackdemo.StartActivity$LockTask.doInBackground(StartActivity.kt:107)
 - locked <0x0f8c80b0> (a java.lang.Object)
```

我们看到正是这个AsyncTask在107行持有锁对象`0x0f8c80b0 `，导致主线程无法获取锁而阻塞，最终导致ANR。

![AsyncTaskHeldLock.png](https://upload-images.jianshu.io/upload_images/3611193-dafe2468abe5e32e.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

### 4.主线程与其他线程之间发生死锁

```
val resourceFirst = "resourceFirst"
val resourceSecond = "resourceSecond"

private fun mockDeadLock() {
    //启动一个后台线程
    thread(start = false) {
        synchronized(resourceSecond) {
            Log.d(TAG, "工作线程获取了锁 resourceSecond")
            Thread.sleep(100)
            Log.d(TAG, "工作线程尝试获取锁 resourceFirst")
            synchronized(resourceFirst) {
                while (true) {
                    Log.d(TAG, "工作线程 mockDeadLock")
                }
            }
        }
    }.start()

    //主线程睡眠30ms后开始获取锁
    Thread.sleep(30)

    synchronized(resourceFirst) {
        Log.d(TAG, "主线程获取了锁 resourceFirst")

        Log.d(TAG, "主线程尝试获取锁 resourceSecond")
        synchronized(resourceSecond) {
            Log.d(TAG, "主线程获取了锁 resourceFirst")
            while (true) {
                Log.d(TAG, "主线程 mockDeadLock")
            }
        }
    }
}
```

上面这段代码逻辑：

1. 工作线程先获取锁`resourceSecond`，然后睡眠100ms保证主线程能获取到锁`resourceFirst`。
2. 主线程睡眠30秒后先获取锁`resourceFirst`，然后再尝试获取锁`resourceSecond`，这时候是获取不到的，因为工作线程已经持有锁`resourceSecond`并且不释放。
3. 工作线程睡眠结束以后尝试获取锁`resourceFirst`，这时候是获取不到的，因为主线程持有了锁`resourceFirst`并且不释放。
4. 最终，造成死锁。

调用mockDeadLock()方法以后，多次点击返回键，制造ANR。

**Logcat输出**

```
2020-06-04 15:07:41.246 ? E/ActivityManager: ANR in com.example.android.jetpackdemo (com.example.android.jetpackdemo/.StartActivity)
    PID: 13626
    Reason: Input dispatching timed out (Waiting to send key event because the focused window has not finished processing all of the input events that were previously delivered to it.  Outbound queue length: 0.  Wait queue length: 2.)
    Load: 7.53 / 6.81 / 6.4
    CPU usage from 177565ms to 0ms ago (2020-06-04 15:04:39.715 to 2020-06-04 15:07:37.281):
      11% 2001/system_server: 7.1% user + 4.4% kernel / faults: 68219 minor 37 major
      3.4% 2661/com.android.systemui: 2.8% user + 0.6% kernel / faults: 20555 minor 29 major
      2% 508/logd: 0.9% user + 1.1% kernel / faults: 76 minor
      1.8% 607/surfaceflinger: 1.1% user + 0.7% kernel / faults: 82 minor 1 major
      0% 24463/com.huawei.hwid.persistent: 0% user + 0% kernel / faults: 7819 minor 24 major
      0.9% 2823/com.huawei.systemmanager:service: 0.6% user + 0.2% kernel / faults: 13277 minor 12 major
    //...      
2020-06-04 15:07:41.246 ? E/ActivityManager: CPU usage from 1714ms to 2243ms later (2020-06-04 15:07:38.994 to 2020-06-04 15:07:39.523):
      12% 2001/system_server: 9% user + 3.6% kernel / faults: 8 minor
        10% 2014/ActivityManager: 5.4% user + 5.4% kernel
        1.8% 2399/UEventObserver: 1.8% user + 0% kernel
      1.5% 13652/kworker/u16:7: 0% user + 1.5% kernel
    2.3% TOTAL: 1.1% user + 1.1% kernel
```

上面的Logcat输出并没有关于我们进程的CUP信息，说明我们的进程CPU占用率很低。那么我们继续看一下traces.txt文件。

**traces.txt部分信息**

```
----- pid 13626 at 2020-06-04 15:07:37 -----
Cmd line: com.example.android.jetpackdemo
Build fingerprint: 'HUAWEI/MLA-AL10/HWMLA:7.0/HUAWEIMLA-AL10/C00B364:user/release-keys'
```

通过进程号pid 13626搜索

```

"main" prio=5 tid=1 Blocked
  | group="main" sCount=1 dsCount=0 obj=0x77d21af8 self=0x7fa2ea2a00
  | sysTid=13626 nice=-10 cgrp=default sched=0/0 handle=0x7fa6f4ba98
  | state=S schedstat=( 288564792 6939269 224 ) utm=23 stm=5 core=0 HZ=100
  | stack=0x7fd42e0000-0x7fd42e2000 stackSize=8MB
  | held mutexes=
  at com.example.android.jetpackdemo.StartActivity.mockDeadLock(StartActivity.kt:142)
  - waiting to lock <0x0a43b5c8> (a java.lang.String) held by thread 17
  at com.example.android.jetpackdemo.StartActivity.onClick(StartActivity.kt:70)
  at java.lang.reflect.Method.invoke!(Native method)
  at androidx.appcompat.app.AppCompatViewInflater$DeclaredOnClickListener.onClick(AppCompatViewInflater.java:397)
  at android.view.View.performClick(View.java:5646)
  at android.view.View$PerformClick.run(View.java:22473)
  at android.os.Handler.handleCallback(Handler.java:761)
  at android.os.Handler.dispatchMessage(Handler.java:98)
  at android.os.Looper.loop(Looper.java:156)
  at android.app.ActivityThread.main(ActivityThread.java:6517)
  at java.lang.reflect.Method.invoke!(Native method)
  at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:942)
  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:832)
```

主线程状态是线程状态是Blocked，说明正在等待获取锁对象，等待获取的锁对象`<0x0a43b5c8>`是一个String对象(a java.lang.String)，该对象被线程id为17的线程持有。然后我们搜索这个锁对象。

```
"Thread-2" prio=5 tid=17 Blocked
  | group="main" sCount=1 dsCount=0 obj=0x12c89dc0 self=0x7f931cd000
  | sysTid=13763 nice=0 cgrp=default sched=0/0 handle=0x7f84344450
  | state=S schedstat=( 886406 280365 2 ) utm=0 stm=0 core=0 HZ=100
  | stack=0x7f84242000-0x7f84244000 stackSize=1037KB
  | held mutexes=
  at com.example.android.jetpackdemo.StartActivity$mockDeadLock$1.invoke(StartActivity.kt:127)
  - waiting to lock <0x0ec26674> (a java.lang.String) held by thread 1
  - locked <0x0a43b5c8> (a java.lang.String)
  at com.example.android.jetpackdemo.StartActivity$mockDeadLock$1.invoke(StartActivity.kt:21)
  at kotlin.concurrent.ThreadsKt$thread$thread$1.run(Thread.kt:30)

```

Thread-2，线程状态是Blocked，说明正在等待获取锁对象，等待获取的锁对象`<0x0ec26674>`是一个String对象(a java.lang.String)，这个对象被线程id为1的线程（也就是主线程）持有。并且当前线程持有锁对象`<0x0a43b5c8>`。

最终，主线程和工作线程Thread-2造成死锁，导致应用无响应。

### 5.主线程在对另一个进程进行同步Binder调用，而后者需要很长时间才能返回

我们的代码是实现从客户端的两个EditText中获取两个数字，然后通过Binder调用服务端的方法计算两个数的和返回给客户端，然后客户端讲计算结果展示在界面上。完整代码请参考 [AIDLDemo](https://github.com/humanheima/AIDLDemo/blob/master/aidlclient/src/main/java/com/hm/aidlclient/BaseKnowledgeActivity.java)。

客户端部分代码

```java
private IMyAidlInterface iMyAidlInterface;
    
private ServiceConnection conn = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
         //获取Binder对象
         iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service);
    }
    //...
};
    
public void onClick(View view) {
    switch (view.getId()) {
        case R.id.btn_count:
            mNum1 = Integer.parseInt(etNum1.getText().toString());
            mNum2 = Integer.parseInt(etNum2.getText().toString());
            try {
                //在主线程进行同步binder调用
                mTotal = iMyAidlInterface.add(mNum1, mNum2);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(TAG, "onClick: " + e.getMessage());
            }
            editShowResult.setText("mTotal=" + mTotal);
            break;
       }
}
```

服务端部分代码

```
public class IRemoteService extends Service {

    private static final String TAG = "IRemoteService";

    private IBinder iBinder = new IMyAidlInterface.Stub() {
        @Override
        public int add(int num1, int num2) throws RemoteException {
            Log.d(TAG, "remote method add: start sleep thread id =" + Thread.currentThread().getId()+"," +
                    "thread name = "+Thread.currentThread().getName());
            try {
                //睡眠一段时间，然后才进行计算
                Thread.sleep(120000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "remote method add: finish sleep return calculate result");
            return num1 + num2;
        }
    };

    public IRemoteService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }
}
```

注意：我们需要先把Binder服务端运行起来，然后再运行Binder客户端执行相应的方法。

**Logcat输出**

```
2020-06-04 15:49:47.006 2001-2014/? E/ActivityManager: ANR in com.hm.aidlclient (com.hm.aidlclient/.BaseKnowledgeActivity)
    PID: 18096
    Reason: Input dispatching timed out (Waiting to send key event because the focused window has not finished processing all of the input events that were previously delivered to it.  Outbound queue length: 0.  Wait queue length: 2.)
    Load: 7.55 / 7.26 / 6.87
    CPU usage from 755516ms to 0ms ago (2020-06-04 15:37:07.545 to 2020-06-04 15:49:43.062) with 99% awake:
      5.1% 2001/system_server: 3.5% user + 1.5% kernel / faults: 139606 minor 17 major
      1.2% 508/logd: 0.5% user + 0.6% kernel / faults: 35 minor
      0.9% 2661/com.android.systemui: 0.7% user + 0.1% kernel / faults: 13039 minor 4 major
      0.8% 12442/adbd: 0.2% user + 0.6% kernel / faults: 23957 minor
      0.7% 607/surfaceflinger: 0.4% user + 0.3% kernel / faults: 183 minor 2 major
      0.6% 28932/com.huawei.appmarket: 0.5% user + 0.1% kernel / faults: 9311 minor 64 major
      0.4% 24463/com.huawei.hwid.persistent: 0.3% user + 0% kernel / faults: 11607 minor 6 major
      0.5% 24301/fingerprint_log: 0% user + 0.5% kernel
      0.3% 4128/com.google.android.gms: 0.2% user + 0% kernel / faults: 26970 minor 16 major
      //...
2020-06-04 15:49:47.006 2001-2014/? E/ActivityManager: CPU usage from 1701ms to 2232ms later (2020-06-04 15:49:44.762 to 2020-06-04 15:49:45.293):
      28% 2001/system_server: 21% user + 7.2% kernel / faults: 38 minor
        16% 2010/HeapTaskDaemon: 16% user + 0% kernel
        9% 2014/ActivityManager: 1.8% user + 7.2% kernel
        1.8% 2001/system_server: 0% user + 1.8% kernel
        1.8% 2540/NetdConnector: 1.8% user + 0% kernel
      9% 607/surfaceflinger: 9% user + 0% kernel
        5.4% 607/surfaceflinger: 5.4% user + 0% kernel
        1.8% 658/Binder:607_1: 0% user + 1.8% kernel
        1.8% 677/EventThread: 0% user + 1.8% kernel
      7.1% 2661/com.android.systemui: 5.3% user + 1.7% kernel / faults: 38 minor
        8.9% 3614/RenderThread: 7.1% user + 1.7% kernel
      1.3% 508/logd: 1.3% user + 0% kernel
      1.3% 624/mm-pp-dpps: 1.3% user + 0% kernel
        2.7% 717/ABA_THREAD: 1.3% user + 1.3% kernel
      1.5% 15978/mdss_fb0: 0% user + 1.5% kernel
      1.6% 18228/logcat: 0% user + 1.6% kernel
    8.3% TOTAL: 5.8% user + 2.5% kernel

```

Logcat输出的信息中并没有什么有价值的信息。那么我们继续看一下traces.txt文件。

**traces.txt中客户端相关信息**

```
----- pid 18096 at 2020-06-04 15:49:43 -----
Cmd line: com.hm.aidlclient
Build fingerprint: 'HUAWEI/MLA-AL10/HWMLA:7.0/HUAWEIMLA-AL10/C00B364:user/release-keys'

```

通过进程号pid 18096搜索


```
"main" prio=5 tid=1 Native
  | group="main" sCount=1 dsCount=0 obj=0x77d21af8 self=0x7fa2ea2a00
  | sysTid=18096 nice=-10 cgrp=default sched=0/0 handle=0x7fa6f4ba98
  | state=S schedstat=( 464662186 22498334 359 ) utm=38 stm=8 core=0 HZ=100
  | stack=0x7fd42e0000-0x7fd42e2000 stackSize=8MB
  | held mutexes=
  kernel: __switch_to+0x70/0x7c
  kernel: binder_thread_read+0x4cc/0x13f0
  kernel: binder_ioctl+0x53c/0xbcc
  kernel: do_vfs_ioctl+0x570/0x5a8
  kernel: SyS_ioctl+0x60/0x88
  kernel: el0_svc_naked+0x24/0x28
  native: #00 pc 000000000006ad6c  /system/lib64/libc.so (__ioctl+4)
  native: #01 pc 000000000001fa48  /system/lib64/libc.so (ioctl+144)
  native: #02 pc 00000000000555a4  /system/lib64/libbinder.so (_ZN7android14IPCThreadState14talkWithDriverEb+260)
  native: #03 pc 0000000000056388  /system/lib64/libbinder.so (_ZN7android14IPCThreadState15waitForResponseEPNS_6ParcelEPi+352)
  native: #04 pc 000000000004b250  /system/lib64/libbinder.so (_ZN7android8BpBinder8transactEjRKNS_6ParcelEPS1_j+72)
  native: #05 pc 0000000000103354  /system/lib64/libandroid_runtime.so (???)
  native: #06 pc 0000000000b36238  /data/dalvik-cache/arm64/system@framework@boot-framework.oat (Java_android_os_BinderProxy_transactNative__ILandroid_os_Parcel_2Landroid_os_Parcel_2I+196)
  at android.os.BinderProxy.transactNative(Native method)
  at android.os.BinderProxy.transact(Binder.java:617)
  at com.hm.aidlserver.IMyAidlInterface$Stub$Proxy.add(IMyAidlInterface.java:90)
  at com.hm.aidlclient.BaseKnowledgeActivity.onClick(BaseKnowledgeActivity.java:109)
  at com.hm.aidlclient.BaseKnowledgeActivity_ViewBinding$1.doClick(BaseKnowledgeActivity_ViewBinding.java:41)
  at butterknife.internal.DebouncingOnClickListener.onClick(DebouncingOnClickListener.java:22)
  at android.view.View.performClick(View.java:5646)
  at android.view.View$PerformClick.run(View.java:22473)
  at android.os.Handler.handleCallback(Handler.java:761)
  at android.os.Handler.dispatchMessage(Handler.java:98)
  at android.os.Looper.loop(Looper.java:156)
  at android.app.ActivityThread.main(ActivityThread.java:6517)
  at java.lang.reflect.Method.invoke!(Native method)
  at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:942)
  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:832)

```

这里我们看到Binder客户端主线程的状态是`Native`，这个状态是native线程的一个状态，对应java线程的`RUNNABLE`状态。更详细的对应关系可以参考[VMThread.java](https://android.googlesource.com/platform/libcore/+/0806909/libdvm/src/main/java/java/lang/VMThread.java)。然后从上面的信息中我们只看到BinderProxy调用了transactNative()方法，这是一个本地方法，最终会调用服务端Binder对象的transact()方法，实现真正的跨进程通信。除了这些我们没有看到其他有用的信息了。那么我们接下来看一看服务端的一些信息，看看能不能找到一些线索。

**traces.txt中服务端相关信息**

```
----- pid 17773 at 2020-06-04 15:49:43 -----
Cmd line: com.hm.aidlserver
Build fingerprint: 'HUAWEI/MLA-AL10/HWMLA:7.0/HUAWEIMLA-AL10/C00B364:user/release-keys'

```

通过进程号pid 17773搜索

```
"main" prio=5 tid=1 Native
  | group="main" sCount=1 dsCount=0 obj=0x77d21af8 self=0x7fa2ea2a00
  | sysTid=17773 nice=0 cgrp=default sched=0/0 handle=0x7fa6f4ba98
  | state=S schedstat=( 213791882 16481247 206 ) utm=18 stm=3 core=1 HZ=100
  | stack=0x7fd42e0000-0x7fd42e2000 stackSize=8MB
  | held mutexes=
  kernel: __switch_to+0x70/0x7c
  kernel: SyS_epoll_wait+0x2d4/0x394
  kernel: SyS_epoll_pwait+0xc4/0x150
  kernel: el0_svc_naked+0x24/0x28
  native: #00 pc 000000000006ac80  /system/lib64/libc.so (__epoll_pwait+8)
  native: #01 pc 000000000001e21c  /system/lib64/libc.so (epoll_pwait+64)
  native: #02 pc 00000000000181d8  /vendor/lib64/libutils.so (_ZN7android6Looper9pollInnerEi+156)
  native: #03 pc 000000000001808c  /vendor/lib64/libutils.so (_ZN7android6Looper8pollOnceEiPiS1_PPv+60)
  native: #04 pc 00000000000f66dc  /system/lib64/libandroid_runtime.so (_ZN7android18NativeMessageQueue8pollOnceEP7_JNIEnvP8_jobjecti+48)
  native: #05 pc 0000000000b91ec0  /data/dalvik-cache/arm64/system@framework@boot-framework.oat (Java_android_os_MessageQueue_nativePollOnce__JI+140)
  at android.os.MessageQueue.nativePollOnce(Native method)
  at android.os.MessageQueue.next(MessageQueue.java:356)
  at android.os.Looper.loop(Looper.java:138)
  at android.app.ActivityThread.main(ActivityThread.java:6517)
  at java.lang.reflect.Method.invoke!(Native method)
  at com.android.internal.os.ZygoteInit$MethodAndArgsCaller.run(ZygoteInit.java:942)
  at com.android.internal.os.ZygoteInit.main(ZygoteInit.java:832)

```

服务端的进程号是pid 17773，我们看到服务端的主线程中也没有什么线索，不要慌，这里我们似乎忘了一点什么，**那就是服务端的Binder对象是运行在服务端的Binder线程池中的**。那我们怎么找到具体是Binder线程池中的哪个线程呢？其实在traces.txt文件中也是输出了的。

```
----- binder transactions -----
18096:18096(m.hm.aidlclient:m.hm.aidlclient) -> 17773:17788(m.hm.aidlserver:Binder:17773_2) code: 1

----- end binder transactions -----
```

上面这段信息的意思就是，我们是在进程id为18096，内核线程为18096的线程（就是主线程）向进程id为17773，内核线id为17788的线程发起跨进程通信。内核线程id为17788的线程的线程名称是`Binder:17773_2`。那么我们就搜索一下`Binder:17773_2`。搜索结果如下所示：

```
"Binder:17773_2" prio=5 tid=10 Sleeping
  | group="main" sCount=1 dsCount=0 obj=0x32c064c0 self=0x7f9a624800
  | sysTid=17788 nice=-10 cgrp=default sched=0/0 handle=0x7fa0fc3450
  | state=S schedstat=( 3077762 6086666 14 ) utm=0 stm=0 core=6 HZ=100
  | stack=0x7fa0ec9000-0x7fa0ecb000 stackSize=1005KB
  | held mutexes=
  at java.lang.Thread.sleep!(Native method)
  - sleeping on <0x05eea4a7> (a java.lang.Object)
  at java.lang.Thread.sleep(Thread.java:379)
  - locked <0x05eea4a7> (a java.lang.Object)
  at java.lang.Thread.sleep(Thread.java:321)
  at com.hm.aidlserver.IRemoteService$1.add(IRemoteService.java:18)
  at com.hm.aidlserver.IMyAidlInterface$Stub.onTransact(IMyAidlInterface.java:55)
  at android.os.Binder.execTransact(Binder.java:565)
```

这里我们终于发现了原因，我们看到`Binder:17773_2`状态是Sleeping，就是服务端的Binder对象的add()方法内部第18行调用了Thread.sleep方法造成长时间无法返回，从而使客户端方法执行无法结束，最终导致ANR。

![binder_anr.png](https://upload-images.jianshu.io/upload_images/3611193-7c912dc6156fdec2.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)



**总结**：本篇文章列举了几种常见原因造成ANR的示例，并分析了相关日志和traces.txt文件。然而在真实的场景中可能还会有各种稀奇古怪的原因造成ANR，排查起来也会复杂的多，所以最重要的还是防患于未然，在实际的开发过程中尽量避免主线程被长时间阻塞。


**文章有错误之处，欢迎批评指正！**


参考链接：
1. [Capture and read bug reports](https://developer.android.com/studio/debug/bug-report)
2. [Android进阶知识：ANR的定位与解决](https://juejin.im/post/5d171455f265da1b61500d17)
3. [ANR](https://developer.android.google.cn/topic/performance/vitals/anr)
4. [Android性能优化（七）之你真的理解ANR吗？](https://juejin.im/post/58e5bd6dda2f60005fea525c)
5. [Android应用ANR分析](https://www.jianshu.com/p/30c1a5ad63a3)
6. [VMThread.java](https://android.googlesource.com/platform/libcore/+/0806909/libdvm/src/main/java/java/lang/VMThread.java)
7. [Android Binder学习笔记（一）](https://www.jianshu.com/p/5b93038c6dcf)
8. [AIDLDemo](https://github.com/humanheima/AIDLDemo)





