package com.ysj.lib.route.callback

/**
 * 用于获取路由的结果的 callback
 *
 * @author Ysj
 * Create time: 2020/10/4
 */
interface RouteResultCallback<T> {

    fun onResult(result: T?)
}