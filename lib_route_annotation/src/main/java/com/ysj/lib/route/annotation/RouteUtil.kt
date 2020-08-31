package com.ysj.lib.route.annotation

import kotlin.IllegalArgumentException

/*
 * 该文件用于处理路由相关
 *
 * @author Ysj
 * Create time: 2020/8/5
 */

/**
 * 检查路由的 path 格式是否正确
 *
 * @param path 要检查的 path
 */
fun checkRouterPath(path: String?) {
    if (path.isNullOrEmpty())
        throw IllegalArgumentException("path 值为空")
    if (!path.startsWith("/"))
        throw IllegalArgumentException("path 值不正确，开头必须为 /")
    if (path.length < 2 || path.split("/").size > 3)
        throw IllegalArgumentException("path 值格式不正确，参考：/group/path")
}

/**
 * 从路由的 path 中截取出 group
 *
 * @param path 要截取的 path
 */
fun subGroupFromPath(path: String?): String {
    checkRouterPath(path)
    val groupStartIndex = path?.indexOf("/", 1)
    if (groupStartIndex == null || groupStartIndex == -1)
        throw IllegalArgumentException("path 中缺少 group")
    return path.substring(1, groupStartIndex)
}