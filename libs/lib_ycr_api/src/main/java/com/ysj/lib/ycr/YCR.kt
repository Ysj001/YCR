package com.ysj.lib.ycr

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ysj.lib.ycr.annotation.*
import com.ysj.lib.ycr.callback.InterceptorCallback
import com.ysj.lib.ycr.entity.ActivityResult
import com.ysj.lib.ycr.entity.InterruptReason
import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.exception.IYCRExceptions
import com.ysj.lib.ycr.exception.YCRExceptionFactory
import com.ysj.lib.ycr.lifecycle.ActivityResultFragment
import com.ysj.lib.ycr.template.IInterceptor
import com.ysj.lib.ycr.template.IProviderParam
import com.ysj.lib.ycr.template.IProviderRoute
import com.ysj.lib.ycr.template.YCRTemplate
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
        if (obj == null) return
        val injector: IProviderParam = getTemplateInstance(
            obj.javaClass.name + SUFFIX_ROUTE_PARAM
        ) ?: return
        injector.injectParam(obj)
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
        val context = postman.getContext() ?: return
        when (postman.types) {
            RouteTypes.ACTIVITY -> {
                val intent = Intent()
                    .addFlags(postman.flags)
                    .putExtras(postman.bundle)
                    .setComponent(ComponentName(postman.applicationId, postman.className))
                runOnMainThread {
                    try {
                        if (context !is Activity) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent, postman.optionsCompat)
                            return@runOnMainThread
                        }
                        if (postman.routeResultCallbacks == null || postman.requestCode < 0) {
                            context.startActivityForResult(
                                intent,
                                postman.requestCode,
                                postman.optionsCompat
                            )
                            if (postman.enterAnim != -1 && postman.exitAnim != -1) context.overridePendingTransition(
                                postman.enterAnim,
                                postman.exitAnim
                            )
                            return@runOnMainThread
                        }
                        val sfm = context.fragmentManager
                        var fragment = sfm.findFragmentByTag(ActivityResultFragment.TAG)
                        if (fragment === null) {
                            fragment = ActivityResultFragment()
                            fragment.listener = { rqc: Int, rsc: Int, data: Intent? ->
                                resultCallback(ActivityResult(rqc, rsc, data))
                            }
                            sfm.beginTransaction().add(fragment, ActivityResultFragment.TAG)
                                .commitAllowingStateLoss()
                            sfm.executePendingTransactions()
                        }
                        fragment.startActivityForResult(
                            intent,
                            postman.requestCode,
                            postman.optionsCompat
                        )
                        if (postman.enterAnim != -1 && postman.exitAnim != -1) context.overridePendingTransition(
                            postman.enterAnim,
                            postman.exitAnim
                        )
                    } catch (e: Exception) {
                        callException(
                            postman,
                            YCRExceptionFactory.navigationException(e)
                        )
                    }
                }
            }
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

    private fun callException(postman: Postman, exception: IYCRExceptions) {
        if (postman.exceptionCallback?.handleException(postman, exception) == true) return
        Caches.globalExceptionProcessors.forEach {
            if (it.handleException(postman, exception)) return
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : YCRTemplate> getTemplateInstance(className: String): T? {
        try {
            return Class.forName(className).getConstructor().newInstance() as T
        } catch (e: Exception) {
            Log.d("YCR-DEV", "$className 没有在该进程找到 --> ${e.message}")
        }
        return null
    }
}