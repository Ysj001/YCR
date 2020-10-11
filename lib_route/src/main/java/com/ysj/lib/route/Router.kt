package com.ysj.lib.route

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.util.Log
import com.ysj.lib.route.annotation.PACKAGE_NAME_ROUTE
import com.ysj.lib.route.annotation.PREFIX_ROUTE
import com.ysj.lib.route.annotation.RouteTypes
import com.ysj.lib.route.annotation.subGroupFromPath
import com.ysj.lib.route.callback.InterceptorCallback
import com.ysj.lib.route.entity.InterruptReason
import com.ysj.lib.route.entity.Postman
import com.ysj.lib.route.remote.*
import com.ysj.lib.route.template.IActionProcessor
import com.ysj.lib.route.template.IProviderRoute
import com.ysj.lib.route.template.RouteTemplate
import java.security.InvalidParameterException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * 路由的管理器
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
class Router private constructor() {

    private object Holder {
        val instance = Router()
    }

    companion object {
        private const val TAG = "Router"

        @JvmStatic
        fun getInstance() = Holder.instance
    }

    fun build(path: String) = Postman(subGroupFromPath(path), path)

    fun navigation(context: Context, postman: Postman) {
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
                    ?: throw InvalidParameterException("找不到路由: ${postman.path}")
            )
            val interrupt = handleInterceptor(context, postman)
            if (interrupt) return
            val routeResult = handleRoute(context, postman)
            postman.routeResultCallback?.onResult(routeResult)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleInterceptor(context: Context, postman: Postman): Boolean {
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
            it.onIntercept(context, postman, object : InterceptorCallback {

                var isFinished = false

                override fun onContinue(postman: Postman) = safeHandle {
                    postman.continueCallback?.onContinue(postman)
                    countDownLatch.countDown()
                }

                override fun onInterrupt(postman: Postman, reason: InterruptReason<*>) =
                    safeHandle {
                        postman.interruptCallback?.onInterrupt(postman, reason)
                        interrupt = true
                        countDownLatch.countDown()
                    }

                inline fun safeHandle(block: () -> Unit) {
                    if (isFinished) throw RuntimeException("拦截器重复处理！")
                    block()
                    isFinished = true
                }
            })
        }
        // 等待所有拦截器处理完再返回结果
        countDownLatch.await(timeout, TimeUnit.MILLISECONDS)
        return interrupt
    }

    private fun handleRoute(context: Context, postman: Postman) =
        when (postman.types) {
            RouteTypes.ACTIVITY -> {
                val intent = Intent()
                intent.component = ComponentName(postman.moduleId, postman.className)
                if (context is Activity) context.startActivityForResult(intent, postman.requestCode)
                else context.startActivity(intent.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
            RouteTypes.ACTION -> {
                val actionProcessor: IActionProcessor? = Caches.actionCache[postman.className]
                    ?: getTemplateInstance(postman.className)
                if (actionProcessor == null) doRemoteAction(
                    postman.moduleId,
                    postman.className,
                    postman.actionName
                )
                else {
                    Caches.actionCache[postman.className] = actionProcessor
                    actionProcessor.doAction(postman.actionName)
                }
            }
            else -> throw InvalidParameterException("该路由类型不正确: ${postman.types}")
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

                        override fun onContinue() = safeHandle {
                            postman.continueCallback?.onContinue(postman)
                        }

                        override fun onInterrupt(param: RemoteParam) = safeHandle {
                            postman.interruptCallback?.onInterrupt(
                                postman,
                                param.params[REMOTE_INTERRUPT_REASON] as InterruptReason<*>
                            )
                            interrupt = true
                        }

                        inline fun safeHandle(block: () -> Unit) {
                            if (isFinished) throw RuntimeException("拦截器重复处理！")
                            block()
                            isFinished = true
                        }
                    }
                )
                remainingTimeout -= System.currentTimeMillis() - oldTime
            }
        return interrupt
    }

    private fun doRemoteAction(applicationId: String, className: String, actionName: String) =
        RemoteRouteProvider.instance
            ?.getRouteService(applicationId)
            ?.doAction(className, actionName)
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