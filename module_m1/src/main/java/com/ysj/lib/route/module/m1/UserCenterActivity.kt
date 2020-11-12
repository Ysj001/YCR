package com.ysj.lib.route.module.m1

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.base.mock.MockUserLogin
import com.ysj.lib.ycr.YCR
import com.ysj.lib.ycr.annotation.Route
import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.exception.IYCRExceptions
import kotlinx.android.synthetic.main.module_m1_activity_user_center.*

/**
 * 用户中心页
 *
 * @author Ysj
 * Create time: 2020/11/7
 */
@Route("/m1/UserCenterActivity")
class UserCenterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_m1_activity_user_center)
        refreshUserInfo()
    }

    fun onLogoutClicked(view: View) {
        YCR.getInstance()
            .build("/base/MockUserLogin")
            .withRouteAction("logout")
            .doOnFinished(Runnable(::refreshUserInfo))
            .doOnException(::doOnException)
            .navigation(this)
    }

    fun onSetAgeClicked(view: View) {
        // 演示通过拦截器自动登录并修改年龄
        YCR.getInstance()
            .build("/base/MockUserLogin")
            .withRouteAction("setAge")
            .withInt("age", 18)
            .doOnFinished(Runnable(::refreshUserInfo))
            .doOnException(::doOnException)
            .navigation(this)
    }

    private fun doOnException(postman: Postman, e: IYCRExceptions): Boolean {
        e.printStackTrace()
        return false
    }

    private fun refreshUserInfo() {
        YCR.getInstance()
            .build("/base/MockUserLogin")
            .withRouteAction("userInfo")
            .useGreenChannel()
            .addOnResultCallback { userInfo: MockUserLogin.UserInfo? ->
                runOnUiThread {
                    tvLoginState.text = userInfo?.userName ?: "未登录"
                    tvAge.text = userInfo?.age?.toString() ?: "未登录"
                }
            }
            .doOnException(::doOnException)
            .navigation(this)
    }
}