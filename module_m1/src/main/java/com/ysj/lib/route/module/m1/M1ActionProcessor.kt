package com.ysj.lib.route.module.m1

import android.util.Log
import com.ysj.lib.route.annotation.Route
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

    override fun doAction(actionName: String): Any? {
        return when (actionName) {
            "m1_test_action1" -> "do m1_test_action1 success！"
            else -> Log.i(TAG, "doAction: $actionName")
        }
    }

}