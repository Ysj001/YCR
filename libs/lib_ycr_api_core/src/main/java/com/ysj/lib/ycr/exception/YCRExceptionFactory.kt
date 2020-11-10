package com.ysj.lib.ycr.exception

/**
 * 构建 YCR 异常的工厂
 *
 * @author Ysj
 * Create time: 2020/10/26
 */
internal object YCRExceptionFactory {

    fun routePathException(msg: String = "") =
        YCRParameterException(ROUTE_PATH_NOT_FOUND, "找不到路由：$msg")

    fun routeTypeException(msg: String = "") =
        YCRParameterException(ROUTE_TYPE_ERROR, "路由类型不正确: $msg")

    fun interceptorRepeatProcessException(msg: String = "") =
        YCRRuntimeException(INTERCEPTOR_REPEAT_PROCESS, "拦截器重复处理了：$msg")

    fun interceptorTimeOutException(count: Long) =
        YCRRuntimeException(INTERCEPTOR_TIME_OUT, "拦截器处理超时了：剩余 $count 个未处理")

    fun doOnInterruptException(case: Throwable) =
        YCRRuntimeException(HANDLE_EXCEPTION_ON_INTERRUPT, case.message ?: "", case)

    fun doOnResultException(case: Throwable) =
        YCRRuntimeException(HANDLE_EXCEPTION_ON_RESULT, case.message ?: "", case)

    fun navigationException(case: Throwable) =
        YCRRuntimeException(NAVIGATION_EXCEPTION, case.message ?: "", case)

    fun exception(case: Throwable): IYCRExceptions = when (case) {
        is YCRRuntimeException -> case
        is YCRParameterException -> case
        else -> YCRExternalException(case)
    }
}