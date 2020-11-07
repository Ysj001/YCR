package com.ysj.lib.route.module.m1

import com.ysj.lib.base.mock.MockUserLogin
import com.ysj.lib.route.YCR
import com.ysj.lib.route.callback.InterceptorCallback
import com.ysj.lib.route.entity.InterruptReason
import com.ysj.lib.route.entity.Postman
import com.ysj.lib.route.template.IInterceptor

/**
 * 登录的拦截器
 *
 * @author Ysj
 * Create time: 2020/11/7
 */
class LoginInterceptor : IInterceptor {

    companion object {
        /** 未登录的拦截码 */
        const val INTERRUPT_CODE_NOT_LOGIN = -1
    }

    override fun priority(): Short = 0

    override fun onIntercept(postman: Postman, callback: InterceptorCallback) {
        demoHandle1(postman, callback)
        demoHandle2(postman, callback)
    }

    // 演示没登录时中断路由并提示
    private fun demoHandle1(postman: Postman, callback: InterceptorCallback) {
        val context = postman.getContext()
        if (postman.path != "/UserCenterActivity" || context == null) {
            callback.onContinue(postman)
            return
        }
        val userInfo = YCR.getInstance()
            .build("/base/MockUserLogin")
            .withRouteAction("userInfo")
            .navigationSync(context) as? MockUserLogin.UserInfo
        if (userInfo == null) {
            callback.onInterrupt(postman, InterruptReason<Any?>(INTERRUPT_CODE_NOT_LOGIN, "请先登录"))
            return
        }
        callback.onContinue(postman)
    }

    private fun demoHandle2(postman: Postman, callback: InterceptorCallback) {

    }
}