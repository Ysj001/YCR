package com.ysj.lib.base.mock

import com.ysj.lib.route.annotation.Route
import com.ysj.lib.route.entity.Postman
import com.ysj.lib.route.template.IActionProcessor
import java.io.Serializable

/**
 * 模拟用户登录
 *
 * @author Ysj
 * Create time: 2020/11/6
 */
@Route("/base/MockUserLogin")
class MockUserLogin : IActionProcessor {

    companion object {
        private const val TAG = "MockUserLogin"

        /** 合法的用户名 */
        const val LEGAL_USER_NAME = "Ysj"

        private var userInfo: UserInfo? = null
    }

    override fun doAction(postman: Postman): Any? {
        val bundle = postman.bundle
        when (postman.actionName) {
            "login" -> {
                val userName = bundle.getString("userName", "")
                val success = userName == LEGAL_USER_NAME
                if (success) userInfo = UserInfo(userName)
                return success
            }
            "logout" -> {

            }
            "userInfo" -> userInfo
        }
        return null
    }

    class UserInfo(
        val userName: String,
        var age: Int = 0
    ) : Serializable

}