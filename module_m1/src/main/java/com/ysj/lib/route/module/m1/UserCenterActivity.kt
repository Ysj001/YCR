package com.ysj.lib.route.module.m1

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.base.mock.MockUserLogin
import com.ysj.lib.route.YCR
import com.ysj.lib.route.annotation.Route
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
            .addOnResultCallback { _: Any? -> refreshUserInfo() }
            .navigation(this)
    }

    fun onSetAgeClicked(view: View) {

    }

    private fun refreshUserInfo() {
        YCR.getInstance()
            .build("/base/MockUserLogin")
            .withRouteAction("userInfo")
            .useGreenChannel()
            .addOnResultCallback { userInfo: MockUserLogin.UserInfo? ->
                tvLoginState.text = userInfo?.userName ?: "未登录"
                tvAge.text = userInfo?.age?.toString() ?: "未登录"
            }
            .doOnException { postman, e ->
                e.printStackTrace()
                false
            }
            .navigation(this)
    }
}