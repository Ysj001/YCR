package com.ysj.lib.route

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.ysj.lib.route.annotation.RouteBean
import com.ysj.lib.route.remote.IRouteService
import com.ysj.lib.route.remote.RemoteParam
import com.ysj.lib.route.remote.RouteService
import com.ysj.lib.route.remote.RouteWrapper
import com.ysj.lib.route.template.IProviderRoute


/**
 * 用于实现路由跨进程通信
 *
 * @author Ysj
 * Create time: 2020/8/17
 */
internal class RouteProvider : ContentProvider() {

    companion object {
        private const val TAG = "RouteProvider"

        /** 主组件的 application id */
        const val mainApplicationId = "com.ysj.lib.router"

        var instance: RouteProvider? = null
    }

    /** 提供给全局获取 [Application] */
    lateinit var application: Application
        private set

    /** 提供给全局获取主模块中的 [IRouteService] */
    lateinit var routeService: IRouteService
        private set

    /** 提供给其他进程获取本进程的 [IRouteService] */
    private lateinit var cursor: Cursor

    override fun onCreate(): Boolean {
        instance = this
        application = context as Application
        initRouteService()
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ) = cursor

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun getType(uri: Uri): String? = null
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ) = 0

    fun getRouteService(applicationId: String): IRouteService? {
        return application.contentResolver
            .query(
                Uri.parse("content://${applicationId}.RouteProvider"),
                null, null, null, null
            )
            ?.let {
                val routerService = IRouteService.Stub
                    .asInterface(it.extras.getBinder(RouteService.ROUTE_SERVICE))
                it.close()
                routerService
            }
    }

    /** 用于插装调用的方法 */
    private fun registerRouteGroup(routeProvider: IProviderRoute) {
        val map = HashMap<String, RouteBean>()
        routeProvider.loadInto(map)
        registerRouteGroup(map)
    }

    /** 注册路由组 */
    private fun registerRouteGroup(map: Map<String, RouteBean>) {
        try {
            val remoteParam = RemoteParam()
            var group = ""
            for (entry in map) {
                remoteParam.params[entry.key] = RouteWrapper(entry.value)
                if (group.isEmpty()) group = entry.value.group
            }
            if (group.isEmpty()) return
            routeService.registerRouteGroup(group, remoteParam)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "register route group failure")
        }
    }

    /** 初始化主模块的 [IRouteService] */
    private fun initRouteService() {
        val routeService = RouteService()
        this.routeService = if (application.packageName == mainApplicationId) routeService
        else getRouteService(mainApplicationId)!!
        cursor = object : MatrixCursor(arrayOf(RouteService.ROUTE_SERVICE)) {
            override fun getExtras() = Bundle().apply {
                putBinder(RouteService.ROUTE_SERVICE, routeService as IBinder)
            }
        }
    }

}