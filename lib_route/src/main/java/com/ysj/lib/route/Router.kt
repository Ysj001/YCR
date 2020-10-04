package com.ysj.lib.route

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ysj.lib.route.annotation.*
import com.ysj.lib.route.remote.REMOTE_ACTION_RESULT
import com.ysj.lib.route.template.IActionProcessor
import com.ysj.lib.route.template.IProviderRoute
import com.ysj.lib.route.template.Template
import java.security.InvalidParameterException

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
            val routeBean = (routes[postman.path]
                ?: findRouteBean(postman.group, postman.path)
                ?: throw InvalidParameterException("找不到路由: ${postman.path}"))
            // TODO: 拦截器
            val routeResult = handleRoute(context, routeBean, postman)
            postman.routeResultCallback?.onResult(routeResult)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun handleRoute(context: Context, routeBean: RouteBean, postman: Postman) =
        when (routeBean.types) {
            RouteTypes.ACTIVITY -> {
                val intent = Intent()
                intent.component = ComponentName(routeBean.moduleId, routeBean.className)
                if (context is Activity) context.startActivityForResult(intent, postman.requestCode)
                else context.startActivity(intent.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
            RouteTypes.ACTION -> {
                val actionProcessor: IActionProcessor? = Caches.actionCache[routeBean.className]
                    ?: getTemplateInstance(routeBean.className)
                if (actionProcessor == null) doRemoteAction(
                    routeBean.moduleId,
                    routeBean.className,
                    postman.actionName
                )
                else {
                    Caches.actionCache[routeBean.className] = actionProcessor
                    actionProcessor.doAction(postman.actionName)
                }
            }
            else -> throw InvalidParameterException("该路由类型不正确: ${routeBean.types}")
        }

    private fun doRemoteAction(applicationId: String, className: String, actionName: String) =
        RouteProvider.instance?.getRouteService(applicationId)?.doAction(className, actionName)
            ?.params?.get(REMOTE_ACTION_RESULT)

    private fun findRouteBean(group: String, path: String) = RouteProvider.instance
        ?.routeService?.findRouteBean(group, path)?.routeBean

    private fun <T : Template> getTemplateInstance(className: String): T? {
        try {
            return Class.forName(className).getConstructor().newInstance() as T
        } catch (e: Exception) {
            Log.d(TAG, "$className 没有在该进程找到 --> ${e.message}")
        }
        return null
    }
}