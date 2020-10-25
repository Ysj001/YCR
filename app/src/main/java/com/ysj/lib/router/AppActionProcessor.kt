package com.ysj.lib.router

import android.util.Log
import com.ysj.lib.route.annotation.Route
import com.ysj.lib.route.entity.Postman
import com.ysj.lib.route.template.IActionProcessor

/**
 *
 *
 * @author Ysj
 * Create time: 2020/10/1
 */
@Route("/app/actions")
class AppActionProcessor : IActionProcessor {

    private val TAG = "AppActionProcessor"

    override fun doAction(postman: Postman): Any? {
        Log.i(TAG, "doAction: ${postman.actionName}")
        return when (postman.actionName) {
            "app_test_action" -> {

            }
            else -> null
        }
    }

}