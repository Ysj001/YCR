package com.ysj.lib.ycr

import android.os.Handler
import android.os.Looper
import com.ysj.lib.ycr.annotation.*
import com.ysj.lib.ycr.callback.InterceptorCallback
import com.ysj.lib.ycr.entity.InterruptReason
import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.exception.IYCRExceptions
import com.ysj.lib.ycr.exception.YCRExceptionFactory
import com.ysj.lib.ycr.template.IInterceptor
import com.ysj.lib.ycr.template.IProviderRoute
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock

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
                var routeBean = routes[fullPath]
                if (routeBean == null) {
                    getTemplateInstance<IProviderRoute>(
                        "${PACKAGE_NAME_ROUTE}.${PREFIX_ROUTE}${postman.group}"
                    )?.loadInto(routes)
                    routeBean = routes[fullPath]
                        ?: throw YCRExceptionFactory.routePathException(fullPath)
                }
                postman.from(routeBean)
            }
            if (postman.greenChannel || !handleInterceptor(postman)) handleRoute(postman) { result ->
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
            RouteTypes.ACTION -> sync {
                val actionProcessor = Caches.actionCache[postman.className]
                    ?: getTemplateInstance(postman.className)
                    ?: throw YCRExceptionFactory.routePathException("/${postman.group}${postman.path}")
                Caches.actionCache[postman.className] = actionProcessor
                resultCallback(actionProcessor.doAction(postman))
            }
            else -> throw YCRExceptionFactory.routeTypeException(postman.types.toString())
        }
    }

    private fun handleInterceptor(postman: Postman): Boolean {
        postman.getContext() ?: return true
        val isMainTH = Thread.currentThread() == Looper.getMainLooper().thread
        // 拦截器超时时间
        val timeout = if (isMainTH) INTERCEPTOR_TIME_OUT_MAIN_TH else INTERCEPTOR_TIME_OUT_SUB_TH
        // 取得匹配的拦截器
        val interceptors = Caches.interceptors
        val countDownLatch = CountDownLatch(interceptors.size)
        val interrupt = AtomicBoolean(false)
        executeInterceptor(
            postman,
            countDownLatch,
            interrupt,
            interceptors.iterator()
        )
        // 等待所有拦截器处理完再返回结果
        countDownLatch.await(timeout, TimeUnit.MILLISECONDS)
        val remaining = countDownLatch.count
        if (remaining != 0L) throw YCRExceptionFactory.interceptorTimeOutException(remaining)
        return interrupt.get()
    }

    private fun executeInterceptor(
        postman: Postman,
        countDownLatch: CountDownLatch,
        interrupt: AtomicBoolean,
        interceptors: Iterator<IInterceptor>
    ) {
        if (postman.isDestroy) {
            while (countDownLatch.count > 0) countDownLatch.countDown()
            return
        }
        if (!interceptors.hasNext()) return
        interceptors.next().onIntercept(postman, object : InterceptorCallback {

            var isFinished = false

            override fun onContinue(postman: Postman) = safeHandle {
                countDownLatch.countDown()
                executeInterceptor(postman, countDownLatch, interrupt, interceptors)
            }

            override fun onInterrupt(postman: Postman, reason: InterruptReason<*>) =
                safeHandle {
                    postman.interruptCallback?.run {
                        try {
                            onInterrupt(postman, reason)
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
                    interceptors.toString()
                )
                block()
                isFinished = true
            }
        })
    }

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
        Caches.globalExceptionProcessors.forEach {
            if (it.handleException(postman, exception)) return
        }
    }

}