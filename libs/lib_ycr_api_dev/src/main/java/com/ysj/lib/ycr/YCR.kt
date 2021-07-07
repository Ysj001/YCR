package com.ysj.lib.ycr

import android.os.Handler
import android.os.Looper
import com.ysj.lib.ycr.annotation.Route
import com.ysj.lib.ycr.annotation.RouteParam
import com.ysj.lib.ycr.annotation.RouteTypes
import com.ysj.lib.ycr.annotation.subGroupFromPath
import com.ysj.lib.ycr.entity.InterruptReason
import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.exception.IYCRExceptions
import com.ysj.lib.ycr.exception.YCRExceptionFactory
import com.ysj.lib.ycr.remote.*
import com.ysj.lib.ycr.remote.entity.PrioritiableClassInfo
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.collections.HashMap

/**
 * 路由的管理器
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
class YCR private constructor() {

    private object Holder {
        val instance = YCR()
    }

    companion object {

        @JvmStatic
        fun getInstance() = Holder.instance

        private fun getCustomExecutor(): ThreadPoolExecutor? = null
    }

    // 主线程 handler
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    // 子线程执行器
    private val executor: ThreadPoolExecutor by lazy {
        getCustomExecutor()
            ?: ThreadPoolExecutor(
                1, 1,
                0L, TimeUnit.MILLISECONDS,
                LinkedBlockingQueue<Runnable>(),
                YCRThreadFactory("default")
            )
    }

    private val lock = ReentrantLock()

    /**
     * 开始构建路由过程
     *
     * @param path 路由的地址，对应 [Route.path]
     */
    fun build(path: String): Postman {
        val group = subGroupFromPath(path)
        return Postman(group, path.substring(group.length + 1))
    }

    /**
     * 注入参数，配合 [RouteParam] 注解
     *
     * @param obj 要注入参数的对象
     */
    fun inject(obj: Any?) {
        obj ?: return
        inject(obj, null)
    }

    fun navigation(postman: Postman) {
        try {
            sync {
                var routes = Caches.routeCache[postman.group]
                if (routes == null) {
                    routes = HashMap()
                    Caches.routeCache[postman.group] = routes
                }
                val fullPath = "/${postman.group}${postman.path}"
                val routeBean = findRemoteRouteBean(postman.group, fullPath)
                    ?: throw YCRExceptionFactory.routePathException(fullPath)
                routes[fullPath] = routeBean
                postman.from(routeBean)
            }
            if (!handleInterceptor(postman)) handleRoute(postman) { result ->
                postman.routeResultCallbacks?.run {
                    try {
                        forEach { callback -> callback.onResult(result) }
                    } catch (e: Exception) {
                        callException(postman, YCRExceptionFactory.doOnResultException(e))
                    }
                }
            }
            postman.finishedCallback?.onFinished()
        } catch (e: Exception) {
            callException(postman, YCRExceptionFactory.exception(e))
        }
    }

    @Suppress("DEPRECATION")
    private fun handleRoute(postman: Postman, resultCallback: (Any?) -> Unit) {
        when (postman.types) {
            RouteTypes.ACTIVITY -> handleRouteActivity(postman, resultCallback)
            RouteTypes.ACTION -> resultCallback(doRemoteAction(postman))
            else -> throw YCRExceptionFactory.routeTypeException(postman.types.toString())
        }
    }

    private fun handleInterceptor(postman: Postman): Boolean {
        postman.getContext() ?: return true
        val routeProvider = RemoteRouteProvider.instance
        val routeService = routeProvider.getRouteService() ?: return false
        val globalInterceptors = TreeSet<PrioritiableClassInfo>()
            .apply {
                (routeService.allApplicationId.params[REMOTE_ALL_APPLICATION_ID] as Collection<*>)
                    .mapNotNull {
                        routeProvider.getRouteService(it as String)
                            ?.allInterceptors
                            ?.params
                            ?.get(REMOTE_INTERRUPT_INFO) as Collection<*>?
                    }
                    .forEach {
                        it.forEach { ii ->
                            add(ii as PrioritiableClassInfo)
                        }
                    }
            }
        // 取得局部拦截器
        val interceptors = postman.interceptors
        val isMainTH = Thread.currentThread() == Looper.getMainLooper().thread
        // 拦截器超时时间
        val timeout = if (isMainTH) INTERCEPTOR_TIME_OUT_MAIN_TH else postman.interceptorTimeout
        val countDownLatch = CountDownLatch(globalInterceptors.size + (interceptors?.size ?: 0))
        val interrupt = AtomicBoolean(false)
        // 先执行局部拦截器
        if (interceptors != null) executeInterceptor(
            postman,
            countDownLatch,
            interrupt,
            interceptors.iterator()
        )
        if (postman.skipGlobalInterceptor) {
            while (countDownLatch.count > 0) countDownLatch.countDown()
        }
        // 若局部拦截器未拦截，再执行全局拦截器
        if (!interrupt.get() && !postman.skipGlobalInterceptor) executeInterceptor(
            postman,
            routeProvider,
            interrupt,
            countDownLatch,
            globalInterceptors
        )
        // 等待所有拦截器处理完再返回结果
        if (timeout == 0L) countDownLatch.await()
        else countDownLatch.await(timeout, TimeUnit.MILLISECONDS)
        val remaining = countDownLatch.count
        if (remaining != 0L) throw YCRExceptionFactory.interceptorTimeOutException(remaining)
        return interrupt.get()
    }

    private fun executeInterceptor(
        postman: Postman,
        routeProvider: RemoteRouteProvider,
        interrupt: AtomicBoolean,
        countDownLatch: CountDownLatch,
        interceptors: TreeSet<PrioritiableClassInfo>
    ) {
        if (postman.isDestroy) {
            while (countDownLatch.count > 0) countDownLatch.countDown()
            return
        }
        if (interceptors.isEmpty()) return
        val info = interceptors.pollFirst()
        val routeService = routeProvider.getRouteService(info.applicationId)
            ?: throw RuntimeException("远端组件未找到：${info.applicationId}")
        routeService.handleInterceptor(
            RemoteParam().apply {
                params[REMOTE_INTERRUPT_INFO] = info
                params[REMOTE_ROUTE_BEAN] = RemoteRouteBean(postman)
            },
            object : RemoteInterceptorCallback.Stub() {

                var isFinished = false

                override fun onContinue(routeBean: RemoteRouteBean) = safeHandle {
                    postman.from(routeBean.routeBean as Postman)
                    executeInterceptor(
                        postman,
                        routeProvider,
                        interrupt,
                        countDownLatch,
                        interceptors
                    )
                    countDownLatch.countDown()
                }

                override fun onInterrupt(param: RemoteParam) = safeHandle {
                    postman.from((param.params[REMOTE_ROUTE_BEAN] as RemoteRouteBean).routeBean as Postman)
                    postman.interruptCallback?.run {
                        try {
                            onInterrupt(
                                postman,
                                param.params[REMOTE_INTERRUPT_REASON] as InterruptReason<*>
                            )
                        } catch (e: Exception) {
                            callException(
                                postman,
                                YCRExceptionFactory.doOnInterruptException(e)
                            )
                        }
                    }
                    interrupt.set(true)
                    while (countDownLatch.count > 0) countDownLatch.countDown()
                }

                fun safeHandle(block: () -> Unit) {
                    if (isFinished) throw YCRExceptionFactory.interceptorRepeatProcessException(
                        info.toString()
                    )
                    block()
                    isFinished = true
                }
            }
        )
    }

    private fun doRemoteAction(postman: Postman) =
        RemoteRouteProvider.instance
            .getRouteService(postman.applicationId)
            ?.doAction(RemoteRouteBean(postman))
            ?.params?.get(REMOTE_ACTION_RESULT)

    private fun findRemoteRouteBean(group: String, path: String) =
        RemoteRouteProvider.instance.getRouteService()?.findRouteBean(group, path)?.routeBean

    internal fun runOnMainThread(runnable: Runnable) {
        val isMainTH = Thread.currentThread() == Looper.getMainLooper().thread
        if (isMainTH) runnable.run()
        else mainHandler.post(runnable)
    }

    internal fun runOnExecutor(runnable: Runnable) {
        val tf = executor.threadFactory
        val currentThread = Thread.currentThread()
        val isMainTH = currentThread == Looper.getMainLooper().thread
        val isDefault = tf is YCRThreadFactory && tf.namePrefix.startsWith("YCR-default")
        if (isDefault && !isMainTH && executorTaskFull()) runnable.run()
        else executor.execute(runnable)
    }

    private fun executorTaskFull() = executor.poolSize == executor.largestPoolSize
            && executor.taskCount >= executor.poolSize

    private inline fun <R> sync(block: () -> R): R {
        lock.lock()
        try {
            return block()
        } finally {
            lock.unlock()
        }
    }

    internal fun callException(postman: Postman, exception: IYCRExceptions) {
        if (postman.exceptionCallback?.handleException(postman, exception) == true) return
        postman.getContext() ?: return
        val routeProvider = RemoteRouteProvider.instance
        val routeService = routeProvider.getRouteService() ?: return
        TreeSet<PrioritiableClassInfo>()
            .apply {
                (routeService.allApplicationId.params[REMOTE_ALL_APPLICATION_ID] as Collection<*>)
                    .mapNotNull {
                        routeProvider.getRouteService(it as String)
                            ?.allGlobalExceptionProcessors
                            ?.params
                            ?.get(REMOTE_EXCEPTION_PROCESSOR_INFO) as Collection<*>?
                    }
                    .forEach {
                        it.forEach { ii ->
                            add(ii as PrioritiableClassInfo)
                        }
                    }
            }
            .forEach {
                if (routeService.handleExceptionProcessor(RemoteParam().apply {
                        params[REMOTE_EXCEPTION_PROCESSOR_INFO] = it
                        params[REMOTE_YCR_EXCEPTION] = exception
                        params[REMOTE_ROUTE_BEAN] = RemoteRouteBean(postman)
                    })) return
            }
    }

}