1. 当Activity销毁重建的时候，会调用 ActivityThread 的 performDestroyActivity 方法，保存了一个 `Activity.NonConfigurationInstances` 对象在 ActivityClientRecord 对象中(ActivityClientRecord 对象应该是在 ActivityThread prepareRelaunchActivity 方法中创建的 )。
2. 当重新创建Activity的时候，ActivityThread会调用performLaunchActivity方法。内部会调用Activity的 attach 方法并传入了 ActivityClientRecord对象中的`Activity.NonConfigurationInstances` 对象为为当前Activity实例的 `mLastNonConfigurationInstances` 赋值。
3. 接下来在Activity的onCreate方法中，会从Activity的`mLastNonConfigurationInstances.activity`  获取销毁的Activity保存下来的`ViewModelStore`对象。而ViewModel 对象保存了创建过的ViewModel。所以新的Activity获取到的是老的ViewModel对象。

* [ViewModel原理分析](https://www.jianshu.com/p/e5c363255617)