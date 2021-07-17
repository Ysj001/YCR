package com.ysj.lib.route.module.m1

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.base.YCRConst
import com.ysj.lib.base.mock.MockUserLogin
import com.ysj.lib.ycr.YCR
import com.ysj.lib.ycr.annotation.Route
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
            .build(YCRConst.route.base_MockUserLogin)
            .withRouteAction("logout")
            .doOnFinished(::refreshUserInfo)
            .navigation(this)
    }

    fun onSetAgeClicked(view: View) {
        // 演示通过拦截器自动登录并修改年龄
        YCR.getInstance()
            .build(YCRConst.route.base_MockUserLogin)
            .withRouteAction("setAge")
            .withInt("age", 18)
            .doOnFinished(::refreshUserInfo)
            .navigation(this)
    }

    private fun refreshUserInfo() {
        YCR.getInstance()
            .build(YCRConst.route.base_MockUserLogin)
            .withRouteAction("userInfo")
            .addOnResultCallback { userInfo: MockUserLogin.UserInfo? ->
                runOnUiThread {
                    tvLoginState.text = userInfo?.userName ?: "未登录"
                    tvAge.text = userInfo?.age?.toString() ?: "未登录"
                }
            }
            .navigation(this)
    }
}