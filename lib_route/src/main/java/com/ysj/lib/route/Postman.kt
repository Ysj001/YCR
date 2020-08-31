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
class Postman : RouteBean, Serializable {

    // 路由所携带的数据
    private val mBundle = Bundle()

    constructor()
    constructor(group: String, path: String) : super(group, path)

    fun navigation(context: Context) = Router.getInstance().navigation(context, this)

    /**
     * Inserts an int value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value an int
     */
    fun withInt(key: String?, value: Int): Postman {
        mBundle.putInt(key, value)
        return this
    }

    /**
     * Inserts a long value into the mapping of this Bundle, replacing
     * any existing value for the given key.
     *
     * @param key a String, or null
     * @param value a long
     */
    fun withLong(key: String?, value: Long): Postman {
        mBundle.putLong(key, value)
        return this
    }

    /**
     * Inserts a Boolean value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a boolean
     */
    fun withBoolean(key: String?, value: Boolean): Postman {
        mBundle.putBoolean(key, value)
        return this
    }

    /**
     * Inserts a String value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a String, or null
     */
    fun withString(key: String?, value: String?): Postman {
        mBundle.putString(key, value)
        return this
    }

    /**
     * Inserts all mappings from the given Bundle into this Bundle.
     *
     * @param bundle a Bundle
     */
    fun withAll(bundle: Bundle): Postman {
        bundle.putAll(bundle)
        return this
    }

    fun getExtras() = mBundle
}