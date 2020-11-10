package com.ysj.lib.ycr.annotation

/**
 * 用于路由跳转的注解
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class Route(
    /**
     * 路由的路径（必填），如："/app/MainActivity" 会取 app 作为默认的 group
     */
    val path: String,
    /**
     * 路由的组（若不填则自动从 path 中提取）
     */
    val group: String = ""
)