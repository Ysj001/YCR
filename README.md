## YCR —— YComponentRouter

[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.3.72-blue.svg)](https://kotlinlang.org)

目前处于开发中...

进度：

- [x] 路由自动注册
- [ ] 组件间行为调用
- [ ] 路由参数注入
- [x] 开发时跨进程组件调用
- [x] 路由拦截器
- [ ] 对路由过程的异常封装
- [ ] 对路由日志进行封装
- [ ] 在 release 包下去除 remote 相关代码，优化体积



### 前言

YCR  是一个帮助 Android 项目组件化改造的库，其设计灵感来源于另外两个组件化框架（[*ARouter*](https://github.com/alibaba/ARouter)，[*CC*](https://github.com/luckybilly/CC)）



### 项目结构

- YCR
  - app —— 主组件 demo
  - lib_base —— 组件基础库 demo
  - module_m1 —— kotlin 的业务组件 demo 
  - module_java —— java 的业务组件 demo
  - libs —— YCR 库的目录
    - lib_ycr_annoation —— YCR 库所用的注解和路由基础实体
    - lib_ycr_apt —— YCR 库的注解处理器
    - lib_ycr_plugin —— YCR 库的插件
    - lib_ycr_api —— YCR 库的所有 Api



### 如何使用

#### API

YCR 支持流式调用 API，你可以在一行代码中完成组件间调用

```java
// 路由到 @Route(path = ""/app/MainActivity"") 标识的 Activity 中
YCR.getInstance().build("/app/MainActivity").navigation(context)

// 路由过程绑定生命周期，当生命周期状态变更为 Lifecycle.State.DESTROYED 时会中断路由过程
YCR.getInstance()
    .build("/app/MainActivity")
    .bindLifecycle(lifecycle)
    .navigation(context)

// 路由时携带参数（如下为传递整个 Bundle）
Bundle params = new Bundle();
YCR.getInstance()
    .build("/app/MainActivity")
    .withAll(params)
    .navigation(context)

// 路由时 Activity 时指定 requestCode，此时 Context 需为 Activity 才能生效
YCR.getInstance()
    .build("/app/MainActivity")
    .withRequestCode(100)
    .navigation(context)
    
// 路由到 @Route(path = "/app/actions") 标识的 IActionProcessor 中
// 并执行 app_test_action 行为
YCR.getInstance()
    .build("/app/actions")
    .withRouteAction("app_test_action")
    .navigation(context)
    
// 添加路由结果的监听
YCR.getInstance()
    .build("/app/actions")
    .withRouteAction("app_test_action")
    .addOnResultCallback(new RouteResultCallback<String>() {
        @Override
        public void onResult(@Nullable String result) {}
    })
    .navigation(context)
    
// 添加路由过程被拦截器中断的监听
YCR.getInstance()
    .build("/app/MainActivity")
    .doOnInterrupt(new InterceptorCallback.InterruptCallback() {
        @Override
        public void onInterrupt(@NotNull Postman postman, @NotNull InterruptReason<?> reason) {}
    })
    .navigation(context)
    
// 添加被拦截器拦截到，但是继续执行的监听
YCR.getInstance()
    .build("/app/MainActivity")
    .doOnContinue(new InterceptorCallback.ContinueCallback() {
        @Override
        public void onContinue(@NotNull Postman postman) {}
    })
    .navigation(context)
```



### 可能遇到的问题



#### 在调用路由后卡顿

这通常有 2 种原因

1. 在主线程中调用时由于等待拦截器处理结果过长造成卡顿

   解决方法：在子线程中调用，或减少拦截器等待时间。

2. 在跨进程调用时长时间卡住，并且通过调试工具 dump 时显示类似如下

   ![asset](assets\problem_1.jpg)

   解决方法：这是由于 binder 长时间等待目标返回结果造成的。这是由于目标进程进入后台时间过长后被系统挂起，只需要手动重新唤起目标即可。在使用模拟器进行跨进程调用则不会出现，这可能与手机厂商的系统调度有关，因此这也是跨进程调用功能只是作为开发时辅助的原因。