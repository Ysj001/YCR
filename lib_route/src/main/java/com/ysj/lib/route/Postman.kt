package com.ysj.lib.route

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import com.ysj.lib.route.annotation.RouteBean
import java.io.Serializable

/**
 * 用于构建路由的参数
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
class Postman(group: String, path: String) : RouteBean(group, path), Serializable {

    /** 路由所携带的数据 */
    val bundle = Bundle()

    /** [Activity] 的 resultCode */
    var resultCode: Int = -1
        private set

    /** 要执行的行为名称 */
    var actionName: String = ""
        private set

    fun <T> navigation(context: Context) = Router.getInstance().navigation<T>(context, this)

    /**
     * 设置要执行的行为
     *
     * @param actionName 要执行的行为的名称
     */
    fun withRouteAction(actionName: String) = apply { this.actionName = actionName }

    fun withResultCode(resultCode: Int) = apply { this.resultCode = resultCode }

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
     * Inserts a Serializable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Serializable object, or null
     */
    fun withSerializable(key: String?, value: Serializable?) =
        apply { bundle.putSerializable(key, value) }

    /**
     * Inserts a Parcelable value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Parcelable object, or null
     */
    fun withParcelable(key: String?, value: Parcelable?) =
        apply { bundle.putParcelable(key, value) }

    /**
     * Inserts an {@link IBinder} value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * <p class="note">You should be very careful when using this function.  In many
     * places where Bundles are used (such as inside of Intent objects), the Bundle
     * can live longer inside of another process than the process that had originally
     * created it.  In that case, the IBinder you supply here will become invalid
     * when your process goes away, and no longer usable, even if a new process is
     * created for you later on.</p>
     *
     * @param key a String, or null
     * @param value an IBinder object, or null
     */
    fun withBinder(key: String?, value: IBinder?) = apply { bundle.putBinder(key, value) }

    /**
     * Inserts all mappings from the given Bundle into this Bundle.
     *
     * @param bundle a Bundle
     */
    fun withAll(bundle: Bundle) = apply { this.bundle.putAll(bundle) }

}