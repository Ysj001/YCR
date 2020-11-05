package com.ysj.lib.base

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
@Route("/base/actions")
class BaseActionProcessor : IActionProcessor {

    private val TAG = "BaseActionProcessor"

    override fun doAction(postman: Postman): Any? {
        Log.i(TAG, "doAction: ${postman.actionName}")
        return null
    }

}