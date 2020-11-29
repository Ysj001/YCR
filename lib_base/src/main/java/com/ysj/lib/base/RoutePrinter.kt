package com.ysj.lib.base

import android.util.Log
import com.ysj.lib.ycr.annotation.RouteTypes
import com.ysj.lib.ycr.callback.InterceptorCallback
import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.template.IGlobalInterceptor

/**
 * 演示拦截器打印路由过程
 *
 * @author Ysj
 * Create time: 2020/11/29
 */
class RoutePrinter : IGlobalInterceptor {

    companion object {
        private const val TAG = "RoutePrinter"
    }

    override fun priority(): Short = Short.MAX_VALUE

    override fun onIntercept(postman: Postman, callback: InterceptorCallback) {
        val context = postman.getContext() ?: return
        if (postman.types == RouteTypes.ACTIVITY) Log.i(
            TAG,
            "from: ${context.javaClass.name} to: ${postman.className}"
        )
        else Log.i(
            TAG,
            "from: ${context.javaClass.name} do: ${postman.className} action: ${postman.actionName}"
        )
        callback.onContinue(postman)
    }
}