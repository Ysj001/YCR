package com.ysj.lib.route

import android.content.Context
import android.os.Bundle
import com.ysj.lib.route.annotation.RouteBean
import java.io.Serializable

/**
 * 用于构建路由的参数
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
class Postman(group: String, path: String) : RouteBean(group, path), Serializable {

    // 路由所携带的数据
    val bundle = Bundle()

    // 行为名称
    var actionName: String = ""
        private set

    fun <T> navigation(context: Context) = Router.getInstance().navigation<T>(context, this)

    /**
     * 执行行为
     *
     * @param actionName 要执行的行为的名称
     */
    fun doAction(actionName: String) = apply { this.actionName = actionName }

    /**
     * Inserts an int value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value an int
     */
    fun withInt(key: String?, value: Int) = apply { bundle.putInt(key, value) }

    /**
     * Inserts a long value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a long
     */
    fun withLong(key: String?, value: Long) = apply { bundle.putLong(key, value) }

    /**
     * Inserts a Boolean value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a boolean
     */
    fun withBoolean(key: String?, value: Boolean) = apply { bundle.putBoolean(key, value) }

    /**
     * Inserts a String value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a String, or null
     */
    fun withString(key: String?, value: String?) = apply { bundle.putString(key, value) }

    /**
     * Inserts all mappings from the given Bundle into this Bundle.
     *
     * @param bundle a Bundle
     */
    fun withAll(bundle: Bundle) = apply { bundle.putAll(bundle) }

}