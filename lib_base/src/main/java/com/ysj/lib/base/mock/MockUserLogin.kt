package com.ysj.lib.base.mock

import android.util.Log
import com.ysj.lib.ycr.annotation.Route
import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.template.IActionProcessor
import java.io.Serializable

/**
 * 模拟用户登录
 *
 * @author Ysj
 * Create time: 2020/11/6
 */
@Route("/base/MockUserLogin")
class MockUserLogin : IActionProcessor, Serializable {

    companion object {
        private const val TAG = "MockUserLogin"

        /** 合法的用户名 */
        const val LEGAL_USER_NAME = "Ysj"
    }

    var userInfo: UserInfo? = null
        private set

    override fun doAction(postman: Postman): Any? {
        Log.i(TAG, "doAction: ${postman.actionName}")
        val bundle = postman.bundle
        return when (postman.actionName) {
            "this" -> this
            "login" -> login(bundle.getString("userName", ""))
            "logout" -> logout()
            "userInfo" -> userInfo
            "setAge" -> setAge(bundle.getInt("age"))
            else -> Unit
        }
    }

    fun login(userName: String): Boolean {
        val success = userName == LEGAL_USER_NAME
        if (success) userInfo = UserInfo(userName)
        return success
    }

    fun logout() {
        userInfo = null
    }

    fun setAge(age: Int) {
        userInfo?.age = age
    }

    class UserInfo(
        val userName: String,
        var age: Int = 0
    ) : Serializable{
        override fun toString(): String {
            return "userName=$userName, age=$age"
        }
    }

}