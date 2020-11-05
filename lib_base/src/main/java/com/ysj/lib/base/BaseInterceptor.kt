package com.ysj.lib.base

import android.util.Log
import com.ysj.lib.route.callback.InterceptorCallback
import com.ysj.lib.route.entity.InterruptReason
import com.ysj.lib.route.entity.Postman
import com.ysj.lib.route.template.IInterceptor

/**
 *
 *
 * @author Ysj
 * Create time: 2020/10/5
 */
class BaseInterceptor : IInterceptor {

    private val TAG = "BaseInterceptor"

    override fun priority(): Short = 0

    override fun onIntercept(postman: Postman, callback: InterceptorCallback) {
        Log.i(TAG, "onIntercept")
//        callback.onContinue(postman)
        callback.onInterrupt(postman, InterruptReason<Any>(1))
    }
}