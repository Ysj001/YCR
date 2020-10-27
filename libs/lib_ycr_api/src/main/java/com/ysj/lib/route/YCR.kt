package com.ysj.lib.route

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Looper
import android.util.Log
import com.ysj.lib.route.annotation.*
import com.ysj.lib.route.callback.InterceptorCallback
import com.ysj.lib.route.entity.ActivityResult
import com.ysj.lib.route.entity.InterruptReason
import com.ysj.lib.route.entity.Postman
import com.ysj.lib.route.exception.YCRExceptionFactory
import com.ysj.lib.route.lifecycle.ActivityResultFragment
import com.ysj.lib.route.remote.*
import com.ysj.lib.route.template.IActionProcessor
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

    internal val executor: Executor = Executors.newSingleThreadExecutor()

    /**
     * 开始构建路由过程
     *
     * @param path 路由的地址，对应 [Route.path]
     */
    fun build(path: String) = Postman(subGroupFromPath(path), path)

    fun navigation(postman: Postman) {
        val routeClassName = "${PACKAGE_NAME_ROUTE}.${PREFIX_ROUTE}${postman.group}"
        val routes = Caches.routeCache[postman.group] ?: HashMap()
        try {
            if (routes.isEmpty()) {
                val providerRoute: IProviderRoute? = getTemplateInstance(routeClassName)
                if (providerRoute != null) {
                    providerRoute.loadInto(routes)
                    Caches.routeCache[postman.group] = routes
                }
            }
            postman.from(
                routes[postman.path]
                    ?: findRemoteRouteBean(postman.group, postman.path)
                    ?: throw YCRExceptionFactory.getRoutePathException(postman.path)
            )
            if (!postman.greenChannel && handleInterceptor(postman)) return
            handleRoute(postman) { result ->
                postman.routeResultCallbacks?.forEach { callback -> callback.onResult(result) }
            }
        } catch (e: Exception) {
            postman.exceptionCallback?.handleException(postman, YCRExceptionFactory.getException(e))
        }
    }

    private fun handleInterceptor(postman: Postman): Boolean {
        postman.getContext() ?: return true
        val isMainTH = Thread.currentThread() == Looper.getMainLooper().thread
        // 拦截器超时时间
        var timeout = if (isMainTH) INTERCEPTOR_TIME_OUT_MAIN_TH else INTERCEPTOR_TIME_OUT_SUB_TH
        val oldTime = System.currentTimeMillis()
        var interrupt = handleRemoteInterceptor(postman, timeout)
        timeout -= System.currentTimeMillis() - oldTime
        // 取得匹配的拦截器
        val matchedInterceptor = Caches.interceptors.filter { it.match(postman) }
        val countDownLatch = CountDownLatch(matchedInterceptor.size)
        matchedInterceptor.forEach {
            it.onIntercept(postman, object : InterceptorCallback {

                var isFinished = false

                override fun onContinue(postman: Postman) = safeHandle {
                    postman.continueCallback?.onContinue(postman)
                }

                override fun onInterrupt(postman: Postman, reason: InterruptReason<*>) =
                    safeHandle {
                        postman.interruptCallback?.onInterrupt(postman, reason)
                        interrupt = true
                    }

                fun safeHandle(block: () -> Unit) {
                    if (isFinished) throw YCRExceptionFactory.getInterceptorRepeatProcessException(
                        it.toString()
                    )
                    block()
                    isFinished = true
                    countDownLatch.countDown()
                }
            })
        }
        // 等待所有拦截器处理完再返回结果
        countDownLatch.await(timeout, TimeUnit.MILLISECONDS)
        val remaining = countDownLatch.count
        if (remaining != 0L) throw YCRExceptionFactory.getInterceptorTimeOutException("剩余: $remaining 个未处理")
        return interrupt
    }

    @Suppress("DEPRECATION")
    private fun handleRoute(postman: Postman, resultCallback: (Any?) -> Unit) {
        val context = postman.getContext() ?: return
        when (postman.types) {
            RouteTypes.ACTIVITY -> {
                val intent = Intent()
                    .addFlags(postman.flags)
                    .setComponent(ComponentName(postman.moduleId, postman.className))
                if (context !is Activity) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                } else {
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
                }
            }
            RouteTypes.ACTION -> {
                val actionProcessor: IActionProcessor? = Caches.actionCache[postman.className]
                    ?: getTemplateInstance(postman.className)
                resultCallback(
                    if (actionProcessor == null) {
                        doRemoteAction(postman)
                    } else {
                        Caches.actionCache[postman.className] = actionProcessor
                        actionProcessor.doAction(postman)
                    }
                )
            }
            else -> throw YCRExceptionFactory.getRouteTypeException(postman.types.toString())
        }
    }

    private fun handleRemoteInterceptor(postman: Postman, timeout: Long): Boolean {
        val routeProvider = RemoteRouteProvider.instance
        val routeService = routeProvider?.getRouteService() ?: return false
        var interrupt = false
        var remainingTimeout = timeout
        (routeService.allApplicationId.params[REMOTE_ALL_APPLICATION_ID] as? ArrayList<*>)
            ?.filter { it != routeProvider.application.packageName }
            ?.map { routeProvider.getRouteService(it as String) }
            ?.forEach {
                if (it == null) return@forEach
                val oldTime = System.currentTimeMillis()
                it.handleInterceptor(
                    remainingTimeout,
                    RemoteRouteBean(postman),
                    object : RemoteInterceptorCallback.Stub() {

                        var isFinished = false

                        override fun onContinue(routeBean: RemoteRouteBean) = safeHandle {
                            postman.from(routeBean.routeBean as Postman)
                            postman.continueCallback?.onContinue(postman)
                        }

                        override fun onInterrupt(param: RemoteParam) = safeHandle {
                            postman.from(param.params[REMOTE_ROUTE_BEAN] as Postman)
                            postman.interruptCallback?.onInterrupt(
                                postman,
                                param.params[REMOTE_INTERRUPT_REASON] as InterruptReason<*>
                            )
                            interrupt = true
                        }

                        inline fun safeHandle(block: () -> Unit) {
                            if (isFinished) throw YCRExceptionFactory.getInterceptorRepeatProcessException()
                            block()
                            isFinished = true
                        }
                    }
                )
                remainingTimeout -= System.currentTimeMillis() - oldTime
            }
        return interrupt
    }

    private fun doRemoteAction(postman: Postman) =
        RemoteRouteProvider.instance
            ?.getRouteService(postman.moduleId)
            ?.doAction(RemoteRouteBean(postman))
            ?.params?.get(REMOTE_ACTION_RESULT)

    private fun findRemoteRouteBean(group: String, path: String) =
        RemoteRouteProvider.instance?.getRouteService()?.findRouteBean(group, path)?.routeBean

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