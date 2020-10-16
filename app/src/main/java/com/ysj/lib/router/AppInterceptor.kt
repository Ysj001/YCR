package com.ysj.lib.router

import android.content.Context
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

    private var doM1Action1Count = 0

    override fun match(postman: Postman): Boolean {
        return true
    }

    override fun onIntercept(context: Context, postman: Postman, callback: InterceptorCallback) {
        if (doM1Action1Count > 2) postman.withRouteAction("m1_test_action2")
        if (postman.actionName == "m1_test_action1") doM1Action1Count++
        postman.withString("app_interceptor", "I'm AppInterceptor !")
        callback.onContinue(postman)
    }
}