package com.ysj.lib.route

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ysj.lib.route.annotation.*
import com.ysj.lib.route.callback.InterceptorCallback
import com.ysj.lib.route.entity.ActivityResult
import com.ysj.lib.route.entity.InterruptReason
import com.ysj.lib.route.entity.Postman
import com.ysj.lib.route.exception.IYCRExceptions
import com.ysj.lib.route.exception.YCRExceptionFactory
import com.ysj.lib.route.lifecycle.ActivityResultFragment
import com.ysj.lib.route.template.IInterceptor
import com.ysj.lib.route.template.IProviderRoute
import com.ysj.lib.route.template.RouteTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
        private const val TAG = "YCR"

        @JvmStatic
        fun getInstance() = Holder.instance
    }

    // 主线程 handler
    internal val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    // 子线程执行器
    internal val executor: Executor by lazy { Executors.newSingleThreadExecutor() }

    /**
     * 开始构建路由过程
     *
     * @param path 路由的地址，对应 [Route.path]
     */
    fun build(path: String) = Postman(subGroupFromPath(path), path)

    fun navigation(postman: Postman) {
        var routes = Caches.routeCache[postman.group]
        if (routes == null) {
            routes = HashMap()
            Caches.routeCache[postman.group] = routes
        }
        try {
            var routeBean = routes[postman.path]
            if (routeBean == null) {
                getTemplateInstance<IProviderRoute>(
                    "${PACKAGE_NAME_ROUTE}.${PREFIX_ROUTE}${postman.group}"
                )?.loadInto(routes)
                routeBean = routes[postman.path]
                    ?: throw YCRExceptionFactory.routePathException(postman.path)
            }
            postman.from(routeBean)
            if (!postman.greenChannel && handleInterceptor(postman)) return
            handleRoute(postman) { result ->
                postman.routeResultCallbacks?.run {
                    runOnMainThread {
                        try {
                            forEach { callback -> callback.onResult(result) }
                        } catch (e: Exception) {
                            callException(postman, YCRExceptionFactory.doOnResultException(e))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            runOnMainThread { callException(postman, YCRExceptionFactory.exception(e)) }
        }
    }

    @Suppress("DEPRECATION")
    private fun handleRoute(postman: Postman, resultCallback: (Any?) -> Unit) {
        val context = postman.getContext() ?: return
        when (postman.types) {
            RouteTypes.ACTIVITY -> {
                val intent = Intent()
                    .addFlags(postman.flags)
                    .setComponent(ComponentName(postman.applicationId, postman.className))
                if (context !is Activity) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
                    runOnMainThread {
                        try {
                            if (postman.routeResultCallbacks == null || postman.requestCode < 0) {
                                context.startActivityForResult(intent, postman.requestCode)
                            } else {
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
                                fragment.startActivityForResult(intent, postman.requestCode)
                            }
                        } catch (e: Exception) {
                            callException(
                                postman,
                                YCRExceptionFactory.navigationException(e)
                            )
                        }
                    }
                }
            }
            RouteTypes.ACTION -> {
                val actionProcessor = Caches.actionCache[postman.className]
                    ?: getTemplateInstance(postman.className)
                    ?: throw YCRExceptionFactory.routePathException(postman.path)
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
        val interrupt = executeInterceptor(postman, countDownLatch, interceptors.iterator())
        // 等待所有拦截器处理完再返回结果
        countDownLatch.await(timeout, TimeUnit.MILLISECONDS)
        val remaining = countDownLatch.count
        if (remaining != 0L) throw YCRExceptionFactory.interceptorTimeOutException(remaining)
        return interrupt
    }

    private fun executeInterceptor(
        postman: Postman,
        countDownLatch: CountDownLatch,
        interceptors: Iterator<IInterceptor>
    ): Boolean {
        var interrupt = false
        if (!interceptors.hasNext()) return interrupt
        interceptors.next().onIntercept(postman, object : InterceptorCallback {

            var isFinished = false

            override fun onContinue(postman: Postman) = safeHandle {
                executeInterceptor(postman, countDownLatch, interceptors)
                countDownLatch.countDown()
            }

            override fun onInterrupt(postman: Postman, reason: InterruptReason<*>) =
                safeHandle {
                    postman.interruptCallback?.run {
                        runOnMainThread {
                            try {
                                onInterrupt(postman, reason)
                            } catch (e: Exception) {
                                callException(
                                    postman,
                                    YCRExceptionFactory.doOnInterruptException(e)
                                )
                            }
                        }
                    }
                    interrupt = true
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
        return interrupt
    }

    internal inline fun runOnMainThread(crossinline block: () -> Unit) {
        val isMainTH = Thread.currentThread() == Looper.getMainLooper().thread
        if (isMainTH) block()
        else mainHandler.post { block() }
    }

    private fun callException(postman: Postman, exception: IYCRExceptions) {
        postman.exceptionCallback?.handleException(postman, exception)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : RouteTemplate> getTemplateInstance(className: String): T? {
        try {
            return Class.forName(className).getConstructor().newInstance() as T
        } catch (e: Exception) {
            Log.d(TAG, "$className 没有在该进程找到 --> ${e.message}")
        }
        return null
    }
}