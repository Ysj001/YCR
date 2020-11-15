package com.ysj.lib.ycr

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.ysj.lib.ycr.annotation.Route
import com.ysj.lib.ycr.annotation.RouteTypes
import com.ysj.lib.ycr.annotation.subGroupFromPath
import com.ysj.lib.ycr.entity.ActivityResult
import com.ysj.lib.ycr.entity.InterceptorInfo
import com.ysj.lib.ycr.entity.InterruptReason
import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.exception.IYCRExceptions
import com.ysj.lib.ycr.exception.YCRExceptionFactory
import com.ysj.lib.ycr.lifecycle.ActivityResultFragment
import com.ysj.lib.ycr.remote.*
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
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
            if (postman.greenChannel || !handleRemoteInterceptor(postman)) handleRoute(postman) { result ->
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
        if (interceptors.isEmpty() || postman.isDestroy) return interrupt
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
        postman.exceptionCallback?.handleException(postman, exception)
    }

}