package com.ysj.lib.base.mock

import com.ysj.lib.ycr.YCR
import com.ysj.lib.ycr.callback.InterceptorCallback
import com.ysj.lib.ycr.entity.InterruptReason
import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.template.IGlobalInterceptor

/**
 * 登录的拦截器
 *
 * @author Ysj
 * Create time: 2020/11/7
 */
class LoginInterceptor : IGlobalInterceptor {

    companion object {
        /** 未登录的拦截码 */
        const val INTERRUPT_CODE_NOT_LOGIN = -1
    }

    override fun onIntercept(postman: Postman, callback: InterceptorCallback) {
        when (postman.path) {
            "/UserCenterActivity" -> demoHandle1(postman, callback)
            "/MockUserLogin" -> demoHandle2(postman, callback)
            else -> callback.onContinue(postman)
        }
    }

    // 演示没登录时中断路由并提示
    private fun demoHandle1(postman: Postman, callback: InterceptorCallback) {
        val context = postman.getContext() ?: return
        val userInfo = YCR.getInstance()
            .build("/base/MockUserLogin")
            .withRouteAction("userInfo")
            .skipGlobalInterceptor()
            .navigationSync(context) as? MockUserLogin.UserInfo
        if (userInfo != null) {
            callback.onContinue(postman)
            return
        }
        callback.onInterrupt(postman, InterruptReason<Any?>(INTERRUPT_CODE_NOT_LOGIN, "请先登录"))
    }

    // 演示没登录时自动登录
    private fun demoHandle2(postman: Postman, callback: InterceptorCallback) {
        val context = postman.getContext() ?: return
        if (postman.actionName != "setAge") {
            callback.onContinue(postman)
            return
        }
        val userInfo = YCR.getInstance()
            .build("/base/MockUserLogin")
            .withRouteAction("userInfo")
            .skipGlobalInterceptor()
            .navigationSync(context) as? MockUserLogin.UserInfo
        if (userInfo == null) {
            YCR.getInstance()
                .build("/base/MockUserLogin")
                .withRouteAction("login")
                .withString("userName", "Ysj")
                .skipGlobalInterceptor()
                .addOnResultCallback { _: Any? ->
                    YCR.getInstance().navigation(postman)
                    callback.onContinue(postman)
                }
                .navigation(context)
        } else {
            callback.onContinue(postman)
        }
    }
}