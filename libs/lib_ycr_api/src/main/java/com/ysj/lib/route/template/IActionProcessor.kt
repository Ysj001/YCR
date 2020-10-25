package com.ysj.lib.route.template

import android.os.Parcelable
import com.ysj.lib.route.entity.Postman
import java.io.Serializable

/**
 * 用于提供行为
 *
 * 注意：子类必须有无参构造函数
 *
 * @author Ysj
 * Create time: 2020/9/23
 */
interface IActionProcessor : RouteTemplate {

    /**
     * 执行行为
     *
     * @param postman 通过调用 [Postman.actionName] 来获取行为 [Postman.bundle] 来获取参数
     * @return 当类型是 [Serializable] 或 [Parcelable] 的子类时可以在跨进程调试中传输
     */
    fun doAction(postman: Postman): Any?
}