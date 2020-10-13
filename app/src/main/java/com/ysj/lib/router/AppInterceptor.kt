package com.ysj.lib.router

import android.content.Context
import android.util.Log
import com.ysj.lib.route.callback.InterceptorCallback
import com.ysj.lib.route.entity.Postman
import com.ysj.lib.route.template.IInterceptor

/**
 *
 *
 * @author Ysj
 * Create time: 2020/10/5
 */
class AppInterceptor : IInterceptor {

    private val TAG = "AppInterceptor"

    override fun match(postman: Postman): Boolean {
        return true
    }

    override fun onIntercept(context: Context, postman: Postman, callback: InterceptorCallback) {
        Log.i(TAG, "onIntercept: $postman")
        postman.withRouteAction("m1_test_action1")
        callback.onContinue(postman)
    }
}