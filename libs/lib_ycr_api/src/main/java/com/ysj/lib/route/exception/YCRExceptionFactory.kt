package com.ysj.lib.route.exception

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

    fun interceptorTimeOutException(msg: String = "") =
        YCRRuntimeException(INTERCEPTOR_TIME_OUT, "拦截器处理超时了：$msg")

    fun doOnContinueException(case: Throwable) =
        YCRRuntimeException(HANDLE_EXCEPTION_ON_CONTINUE, case.message ?: "", case)

    fun doOnInterruptException(case: Throwable) =
        YCRRuntimeException(HANDLE_EXCEPTION_ON_INTERRUPT, case.message ?: "", case)

    fun doOnResultException(case: Throwable) =
        YCRRuntimeException(HANDLE_EXCEPTION_ON_RESULT, case.message ?: "", case)

    fun exception(case: Throwable): IYCRExceptions = when (case) {
        is YCRRuntimeException -> case
        is YCRParameterException -> case
        else -> YCRExternalException(case)
    }
}