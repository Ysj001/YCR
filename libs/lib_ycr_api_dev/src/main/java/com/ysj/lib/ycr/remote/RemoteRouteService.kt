package com.ysj.lib.ycr.remote

import android.os.Parcelable
import android.util.Log
import com.ysj.lib.ycr.Caches
import com.ysj.lib.ycr.callback.InterceptorCallback
import com.ysj.lib.ycr.entity.InterruptReason
import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.exception.IYCRExceptions
import com.ysj.lib.ycr.remote.entity.PrioritiableClassInfo
import com.ysj.lib.ycr.template.IActionProcessor
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.HashMap

/**
 * 夸进程的路由服务
 *
 * @author Ysj
 * Create time: 2020/8/18
 */
internal class RemoteRouteService : IRouteService.Stub() {

    companion object {
        private const val TAG = "RouteService"

        /** 用于获取路由服务的 KEY */
        const val ROUTE_SERVICE = "ROUTE_SERVICE"
    }

    /** 所有组件的 application id */
    private val allApplicationId = HashSet<String>()

    override fun registerToMainApp(applicationId: String) {
        RemoteRouteProvider.instance.also {
            if (it.mainApplicationId != applicationId) {
                it.routeServiceCache.remove(applicationId)
            }
            allApplicationId.add(applicationId)
        }
    }

    override fun getAllApplicationId() = RemoteParam().also {
        it.params[REMOTE_ALL_APPLICATION_ID] = allApplicationId
    }

    override fun registerRouteGroup(group: String, param: RemoteParam) {
        var routeMap = Caches.routeCache[group]
        if (routeMap == null) {
            routeMap = HashMap()
            Caches.routeCache[group] = routeMap
        }
        param.params.forEach {
            val routeBean = routeMap[it.key]
            if (routeBean == null) routeMap[it.key] = (it.value as RemoteRouteBean).routeBean
        }
        Log.i(TAG, "registerRouteGroup: ${Caches.routeCache.size} , $group , $routeMap")
    }

    override fun findRouteBean(group: String?, path: String?): RemoteRouteBean? {
        if (group.isNullOrEmpty() || path.isNullOrEmpty()) return null
        val routeBean = Caches.routeCache[group]?.get(path)
        return if (routeBean == null) null else RemoteRouteBean(routeBean)
    }

    override fun doAction(remote: RemoteRouteBean): RemoteParam? {
        val postman = remote.routeBean as Postman
        if (postman.className.isEmpty() || postman.actionName.isEmpty()) return null
        try {
            val processor =
                Caches.actionCache[postman.className] ?: Class.forName(postman.className)
                    .getConstructor().newInstance() as IActionProcessor
            Caches.actionCache[postman.className] = processor
            if (postman.getContext() == null) postman.context =
                WeakReference(RemoteRouteProvider.instance.context!!)
            val actionResult = processor.doAction(postman) ?: return null
            if (actionResult !is Serializable && actionResult !is Parcelable) return null
            return RemoteParam().apply { params[REMOTE_ACTION_RESULT] = actionResult }
        } catch (e: Exception) {
            Log.w(TAG, "${postman.className} 行为处理器没有在该进程找到 --> ${e.message}")
        }
        return null
    }

    override fun getAllInterceptors(): RemoteParam = RemoteParam().apply {
        params[REMOTE_INTERRUPT_INFO] = Caches.interceptors.map {
            PrioritiableClassInfo(
                RemoteRouteProvider.instance.context!!.packageName,
                it.javaClass.name,
                it.priority()
            )
        }
    }

    override fun handleInterceptor(param: RemoteParam, callback: RemoteInterceptorCallback) {
        val interceptorInfo = param.params[REMOTE_INTERRUPT_INFO] as PrioritiableClassInfo
        val postman = (param.params[REMOTE_ROUTE_BEAN] as RemoteRouteBean).routeBean as Postman
        if (postman.getContext() == null) postman.context =
            WeakReference(RemoteRouteProvider.instance.context!!)
        // 取得匹配的拦截器
        Caches.interceptors.find { it.javaClass.name == interceptorInfo.className }!!
            .onIntercept(postman, object : InterceptorCallback {
                override fun onContinue(postman: Postman) {
                    callback.onContinue(RemoteRouteBean(postman))
                }

                override fun onInterrupt(postman: Postman, reason: InterruptReason<*>) {
                    callback.onInterrupt(RemoteParam().also { param ->
                        param.params[REMOTE_ROUTE_BEAN] = RemoteRouteBean(postman)
                        param.params[REMOTE_INTERRUPT_REASON] = reason
                    })
                }
            })
    }

    override fun getAllGlobalExceptionProcessors(): RemoteParam = RemoteParam().apply {
        params[REMOTE_EXCEPTION_PROCESSOR_INFO] = Caches.globalExceptionProcessors.map {
            PrioritiableClassInfo(
                RemoteRouteProvider.instance.context!!.packageName,
                it.javaClass.name,
                it.priority()
            )
        }
    }

    override fun handleExceptionProcessor(param: RemoteParam): Boolean {
        val exceptionProcessorInfo = param.params[REMOTE_EXCEPTION_PROCESSOR_INFO] as PrioritiableClassInfo
        val postman = (param.params[REMOTE_ROUTE_BEAN] as RemoteRouteBean).routeBean as Postman
        if (postman.getContext() == null) postman.context =
            WeakReference(RemoteRouteProvider.instance.context!!)
        // 取得匹配的异常处理器
        return Caches.globalExceptionProcessors
            .find { it.javaClass.name == exceptionProcessorInfo.className }!!
            .handleException(postman, param.params[REMOTE_YCR_EXCEPTION] as IYCRExceptions)
    }

}