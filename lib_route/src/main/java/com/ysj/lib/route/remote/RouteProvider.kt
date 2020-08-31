package com.ysj.lib.route.remote

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * 用于实现路由跨进程通信
 *
 * @author Ysj
 * Create time: 2020/8/17
 */
class RouteProvider : ContentProvider() {

    companion object {
        private const val TAG = "RouteProvider"

        /** 可全局使用的 Application */
        lateinit var application: Application
            private set

        fun getMainRouteProviderUri(): Uri {
            return Uri.parse("content://com.ysj.lib.router.RouteProvider")
        }
    }

    override fun onCreate(): Boolean {
        application = context as Application
        return false
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return RouteService.getInstance().cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ) = 0

    override fun getType(uri: Uri): String? = null
}