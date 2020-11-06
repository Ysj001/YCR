package com.ysj.lib.route.remote

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
import com.ysj.lib.route.template.IProviderRoute


/**
 * 用于实现路由跨进程通信
 *
 * @author Ysj
 * Create time: 2020/8/17
 */
internal class RemoteRouteProvider : ContentProvider() {

    companion object {
        private const val TAG = "RemoteRouteProvider"

        var instance: RemoteRouteProvider? = null
    }

    /** 主组件的 application id */
    val mainApplicationId = "It is automatically modified to 'main application id' at compile time"

    /** 提供给全局获取 [Application] */
    lateinit var application: Application
        private set

    /** [IRouteService] 缓存 */
    val routeServiceCache by lazy(LazyThreadSafetyMode.NONE) { HashMap<String, IRouteService>() }

    /** 提供给其他进程获取本进程的 [IRouteService] */
    private lateinit var cursor: Cursor

    override fun onCreate(): Boolean {
        instance = this
        application = context as Application
        Log.i(TAG, "onCreate: $mainApplicationId")
        initRouteService()
        // 注册本组件的 application id 到主组件
        getRouteService()?.registerToMainApp(application.packageName)
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

    /**
     * 获取 [IRouteService] 默认获取主模块中的
     */
    fun getRouteService(applicationId: String = mainApplicationId): IRouteService? {
        return routeServiceCache[applicationId] ?: application.contentResolver
            .query(
                Uri.parse("content://${applicationId}.RouteProvider"),
                null, null, null, null
            )
            ?.let {
                val routerService = IRouteService.Stub
                    .asInterface(it.extras.getBinder(RemoteRouteService.ROUTE_SERVICE))
                routeServiceCache[applicationId] = routerService
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
                remoteParam.params[entry.key] = RemoteRouteBean(entry.value)
                if (group.isEmpty()) group = entry.value.group
            }
            if (group.isEmpty()) return
            getRouteService()!!.registerRouteGroup(group, remoteParam)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "register route group failure")
        }
    }

    /** 初始化主模块的 [IRouteService] */
    private fun initRouteService() {
        val routeService = RemoteRouteService()
        this.routeServiceCache[mainApplicationId] =
            if (application.packageName == mainApplicationId) routeService else getRouteService()!!
        cursor = object : MatrixCursor(arrayOf(RemoteRouteService.ROUTE_SERVICE)) {
            override fun getExtras() = Bundle().apply {
                putBinder(RemoteRouteService.ROUTE_SERVICE, routeService as IBinder)
            }
        }
    }

}