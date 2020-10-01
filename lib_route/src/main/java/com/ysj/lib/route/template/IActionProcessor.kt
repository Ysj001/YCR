package com.ysj.lib.route.template

/**
 * 用于提供行为
 *
 * 注意：子类必须有无参构造函数
 *
 * @author Ysj
 * Create time: 2020/9/23
 */
interface IActionProcessor : Template {

    fun <T> doAction(actionName: String): T
}