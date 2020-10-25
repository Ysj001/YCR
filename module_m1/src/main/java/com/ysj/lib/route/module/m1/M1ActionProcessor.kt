package com.ysj.lib.route.module.m1

import android.util.Log
import com.ysj.lib.route.annotation.Route
import com.ysj.lib.route.entity.Postman
import com.ysj.lib.route.template.IActionProcessor

/**
 *
 *
 * @author Ysj
 * Create time: 2020/9/26
 */
@Route("/m1/actions")
class M1ActionProcessor() : IActionProcessor {

    private val TAG = "M1ActionProcessor"

    override fun doAction(postman: Postman): Any? {
        return when (postman.actionName) {
            "m1_test_action1" -> "do m1_test_action1 success！"
            "m1_test_action2" -> 100
            else -> Log.i(TAG, "doAction: ${postman.actionName}")
        }
    }

}