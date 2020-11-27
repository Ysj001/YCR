package com.ysj.lib.ycr.entity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.ysj.lib.ycr.YCR
import com.ysj.lib.ycr.annotation.RouteBean
import com.ysj.lib.ycr.callback.FinishedCallback
import com.ysj.lib.ycr.callback.InterceptorCallback
import com.ysj.lib.ycr.callback.RouteResultCallback
import com.ysj.lib.ycr.callback.YCRExceptionCallback
import com.ysj.lib.ycr.lifecycle.RouteLifecycleObserver
import com.ysj.lib.ycr.type.checkMethodParameterType
import java.io.Serializable
import java.lang.ref.WeakReference

/**
 * 用于构建路由过程的实体
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
class Postman(group: String, path: String) : RouteBean(group, path), RouteLifecycleObserver {

    /** 路由所携带的数据 */
    val bundle = Bundle()

    /** [Intent.addFlags] */
    var flags: Int = 0
        private set

    /** [Activity] 的 requestCode */
    var requestCode: Int = -1
        private set

    /** 表示是否使用绿色通道，为 true 会跳过拦截器 */
    var greenChannel: Boolean = false
        private set

    /** 要执行的行为名称 */
    var actionName: String = ""
        private set

    /** 可用于判断路由是否被销毁 */
    var isDestroy = false
        internal set

    internal var lifecycle: Lifecycle? = null

    internal var context: WeakReference<Context>? = null

    internal var exceptionCallback: YCRExceptionCallback? = null

    internal var routeResultCallbacks: MutableCollection<RouteResultCallback<Any?>>? = null

    internal var interruptCallback: InterceptorCallback.InterruptCallback? = null

    internal var finishedCallback: FinishedCallback? = null

    override fun onDestroy(owner: LifecycleOwner) {
        owner.lifecycle.removeObserver(this)
        destroy()
    }

    /**
     * 手动销毁路由，会中断路由过程并清除所有回调
     */
    fun destroy() {
        isDestroy = true
        lifecycle?.removeObserver(this)
        context?.clear()
        context = null
        exceptionCallback = null
        routeResultCallbacks = null
        interruptCallback = null
        lifecycle = null
        finishedCallback = null
    }

    /**
     * 绑定生命周期，当生命周期状态变更为 [Lifecycle.State.DESTROYED] 时会中断路由过程
     */
    fun bindLifecycle(lifecycle: Lifecycle) = apply {
        this.lifecycle = lifecycle
        lifecycle.addObserver(this)
    }

    /**
     * 调用该方法启用绿色通道，跳过所有拦截器
     */
    fun useGreenChannel() = apply { this.greenChannel = true }

    /**
     * 路由调用链的最后一步，开始路由导航
     */
    fun navigation(context: Context) {
        this.context = WeakReference(context)
        YCR.getInstance().runOnExecutor { YCR.getInstance().navigation(this) }
    }

    /**
     * 路由调用链的最后一步，开始路由导航（同步的）
     */
    fun navigationSync(context: Context): Any? {
        this.context = WeakReference(context)
        var result: Any? = null
        addOnResultCallback<Any?> { result = it }
        YCR.getInstance().navigation(this)
        return result
    }

    /**
     * 用于获取路由成功的结果，你可以添加多个类型用于适应路由过程中行为的改变
     * - 如果导航过程中被拦截则不会执行
     * - 当添加了多个监听时只会回调类型匹配的那个
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> addOnResultCallback(callback: RouteResultCallback<T?>) = apply {
        if (this.routeResultCallbacks == null) this.routeResultCallbacks = ArrayList()
        this.routeResultCallbacks?.add(object : RouteResultCallback<Any?>() {
            override fun onResult(result: Any?) {
                if (result is Unit) return
                if (result == null) {
                    callback.onResult(result)
                    return
                }
                if (checkMethodParameterType(
                        callback.javaClass,
                        "onResult",
                        result.javaClass
                    )
                ) callback.onResult(result as T?)
            }
        })
    }

    @JvmSynthetic
    @Suppress("UNCHECKED_CAST")
    fun <T> addOnResultCallback(callback: (T?) -> Unit) = apply {
        if (this.routeResultCallbacks == null) this.routeResultCallbacks = ArrayList()
        this.routeResultCallbacks?.add(object : RouteResultCallback<Any?>() {
            override fun onResult(result: Any?) {
                if (result is Unit) return
                if (result == null) {
                    callback(result)
                    return
                }
                if (checkMethodParameterType(
                        callback.javaClass,
                        "invoke",
                        result.javaClass
                    )
                ) callback(result as T?)
            }
        })
    }

    /**
     * 路由调用过程结束的回调
     */
    fun doOnFinished(callback: FinishedCallback) = apply { finishedCallback = callback }

    /**
     * 当路由过程中出现异常时回调
     */
    fun doOnException(callback: YCRExceptionCallback) = apply { this.exceptionCallback = callback }

    /**
     * 当被拦截器拦截，拦截器执行 [InterceptorCallback.onInterrupt] 后回调
     */
    fun doOnInterrupt(callback: InterceptorCallback.InterruptCallback) = apply {
        this.interruptCallback = callback
    }

    /**
     * Set special flags controlling how this intent is handled.
     *
     * @see Intent.setFlags
     * @param flags The desired flags.
     */
    fun withFlags(flags: Int) = apply { this.flags = flags }

    /**
     * 设置要执行的行为
     *
     * @param actionName 要执行的行为的名称
     */
    fun withRouteAction(actionName: String?) = apply {
        if (actionName.isNullOrEmpty()) return@apply
        this.actionName = actionName
    }

    /**
     * 设置 [Activity] 的 requestCode
     *
     * @param requestCode
     * int: If >= 0, this code will be returned in onActivityResult() when the activity exits.
     */
    fun withRequestCode(requestCode: Int) = apply { this.requestCode = requestCode }

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
     * Inserts a Bundle value into the mapping of this Bundle, replacing
     * any existing value for the given key.  Either key or value may be null.
     *
     * @param key a String, or null
     * @param value a Bundle object, or null
     */
    fun withBundle(key: String?, value: Bundle?) = apply { bundle.putBundle(key, value) }

    /**
     * Inserts all mappings from the given Bundle into this Bundle.
     *
     * @param bundle a Bundle
     */
    fun withAll(bundle: Bundle?) = apply {
        if (bundle == null) return@apply
        this.bundle.putAll(bundle)
    }

    /**
     * 获取 [Context] 在以下情况会为 null
     * - 当路由未开始（未调用 [navigation] 或 [navigationSync]）时
     * - 当生命周期状态变更为 [Lifecycle.State.DESTROYED] 时
     * - 当弱引用被回收时
     */
    fun getContext(): Context? = context?.get()

    /**
     * 将另一个 [Postman] 中的数据复制过来
     */
    internal fun from(postman: Postman) {
        withAll(postman.bundle)
        this.actionName = postman.actionName
        this.requestCode = postman.requestCode
        this.flags = postman.flags
        this.greenChannel = postman.greenChannel
        this.isDestroy = postman.isDestroy
        from(postman as RouteBean)
    }

    /**
     * 将 [RouteBean] 的信息赋值到 [Postman] 中
     */
    internal fun from(routeBean: RouteBean) {
        this.group = routeBean.group
        this.path = routeBean.path
        this.types = routeBean.types
        this.typeElement = routeBean.typeElement
        this.applicationId = routeBean.applicationId
        this.className = routeBean.className
    }
}