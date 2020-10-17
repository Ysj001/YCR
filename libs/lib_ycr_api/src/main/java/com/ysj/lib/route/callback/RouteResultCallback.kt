package com.ysj.lib.route.callback

/**
 * 用于获取路由的结果的 callback
 *
 * 由于需要通过泛型对类型结果进行流转，而 java8 的 Lambda 表达式的生成类中去掉了泛型，因此此处使用抽象类而不使用接口
 * 以限制使用 java8 的 Lambda 表达式
 *
 * @author Ysj
 * Create time: 2020/10/4
 */
abstract class RouteResultCallback<T> {

    abstract fun onResult(result: T?)

}