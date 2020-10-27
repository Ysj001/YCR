package com.ysj.lib.route.exception

/**
 * 构建 YCR 异常的工厂
 *
 * @author Ysj
 * Create time: 2020/10/26
 */
internal object YCRExceptionFactory {

    fun getRoutePathException(msg: String = "") =
        YCRParameterException(ROUTE_PATH_NOT_FOUND, "找不到路由：$msg")

    fun getRouteTypeException(msg: String = "") =
        YCRParameterException(ROUTE_TYPE_ERROR, "路由类型不正确: $msg")

    fun getInterceptorRepeatProcessException(msg: String = "") =
        YCRRuntimeException(INTERCEPTOR_REPEAT_PROCESS, "拦截器重复处理了：$msg")

    fun getInterceptorTimeOutException(msg: String = "") =
        YCRRuntimeException(INTERCEPTOR_TIME_OUT, "拦截器处理超时了：$msg")

    fun getException(case: Throwable): IYCRExceptions = when (case) {
        is YCRRuntimeException -> case
        is YCRParameterException -> case
        else -> YCRExternalException(case)
    }
}