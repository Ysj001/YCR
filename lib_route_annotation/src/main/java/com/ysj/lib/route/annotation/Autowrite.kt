package com.ysj.lib.route.annotation

/**
 * 用于注入路由传参的注解
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class Autowrite(
    /**
     * 如果没有传则默认用该注解作用的 field 的名字
     */
    val name: String = ""
)