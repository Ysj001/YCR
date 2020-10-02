package com.ysj.lib.route.template

import android.os.Parcelable
import java.io.Serializable

/**
 * 用于提供行为
 *
 * 注意：子类必须有无参构造函数
 *
 * @author Ysj
 * Create time: 2020/9/23
 */
interface IActionProcessor : Template {

    /**
     * 执行行为
     *
     * @param actionName 行为的名字
     * @return 类型必须是 [Serializable] 或 [Parcelable] 的子类
     */
    fun doAction(actionName: String): Any?
}