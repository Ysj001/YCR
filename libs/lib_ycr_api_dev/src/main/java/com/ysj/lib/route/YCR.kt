package com.ysj.lib.route

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.ysj.lib.route.annotation.Route
import com.ysj.lib.route.annotation.RouteTypes
import com.ysj.lib.route.annotation.subGroupFromPath
import com.ysj.lib.route.entity.ActivityResult
import com.ysj.lib.route.entity.InterceptorInfo
import com.ysj.lib.route.entity.InterruptReason
import com.ysj.lib.route.entity.Postman
import com.ysj.lib.route.exception.IYCRExceptions
import com.ysj.lib.route.exception.YCRExceptionFactory
import com.ysj.lib.route.lifecycle.ActivityResultFragment
import com.ysj.lib.route.remote.*
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
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
            val routeBean = findRemoteRouteBean(postman.group, postman.path)
                ?: throw YCRExceptionFactory.routePathException(postman.path)
            routes[postman.path] = routeBean
            postman.from(routeBean)
            if (!postman.greenChannel && handleRemoteInterceptor(postman)) return
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
            RouteTypes.ACTION -> resultCallback(doRemoteAction(postman))
            else -> throw YCRExceptionFactory.routeTypeException(postman.types.toString())
        }
    }

    private fun handleRemoteInterceptor(postman: Postman): Boolean {
        postman.getContext() ?: return true
        val isMainTH = Thread.currentThread() == Looper.getMainLooper().thread
        // 拦截器超时时间
        val timeout = if (isMainTH) INTERCEPTOR_TIME_OUT_MAIN_TH else INTERCEPTOR_TIME_OUT_SUB_TH
        val routeProvider = RemoteRouteProvider.instance ?: return false
        val routeService = routeProvider.getRouteService() ?: return false
        val interceptors = TreeSet<InterceptorInfo>()
            .apply {
                (routeService.allApplicationId.params[REMOTE_ALL_APPLICATION_ID] as Collection<*>)
                    .mapNotNull {
                        routeProvider.getRouteService(it as String)
                            ?.findInterceptor(RemoteRouteBean(postman))
                            ?.params
                            ?.get(REMOTE_INTERRUPT_INFO) as Collection<*>?
                    }
                    .forEach {
                        it.forEach { ii ->
                            add(ii as InterceptorInfo)
                        }
                    }
            }
        val countDownLatch = CountDownLatch(interceptors.size)
        val interrupt = executeInterceptor(postman, routeProvider, countDownLatch, interceptors)
        // 等待所有拦截器处理完再返回结果
        countDownLatch.await(timeout, TimeUnit.MILLISECONDS)
        val remaining = countDownLatch.count
        if (remaining != 0L) throw YCRExceptionFactory.interceptorTimeOutException(remaining)
        return interrupt
    }

    private fun executeInterceptor(
        postman: Postman,
        routeProvider: RemoteRouteProvider,
        countDownLatch: CountDownLatch,
        interceptors: TreeSet<InterceptorInfo>
    ): Boolean {
        var interrupt = false
        if (interceptors.isEmpty()) return interrupt
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
                    executeInterceptor(postman, routeProvider, countDownLatch, interceptors)
                    countDownLatch.countDown()
                }

                override fun onInterrupt(param: RemoteParam) = safeHandle {
                    postman.from((param.params[REMOTE_ROUTE_BEAN] as RemoteRouteBean).routeBean as Postman)
                    postman.interruptCallback?.run {
                        runOnMainThread {
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
                    }
                    interrupt = true
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
        return interrupt
    }

    private fun doRemoteAction(postman: Postman) =
        RemoteRouteProvider.instance
            ?.getRouteService(postman.applicationId)
            ?.doAction(RemoteRouteBean(postman))
            ?.params?.get(REMOTE_ACTION_RESULT)

    private fun findRemoteRouteBean(group: String, path: String) =
        RemoteRouteProvider.instance?.getRouteService()?.findRouteBean(group, path)?.routeBean

    internal inline fun runOnMainThread(crossinline block: () -> Unit) {
        val isMainTH = Thread.currentThread() == Looper.getMainLooper().thread
        if (isMainTH) block()
        else mainHandler.post { block() }
    }

    private fun callException(postman: Postman, exception: IYCRExceptions) {
        postman.exceptionCallback?.handleException(postman, exception)
    }

}