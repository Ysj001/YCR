package com.ysj.lib.route.exception

import com.ysj.lib.route.annotation.RouteTypes
import java.security.InvalidParameterException

/*
 * 该文件定义了 YCR 的异常类型和常量
 *
 * @author Ysj
 * Create time: 2020/10/26
 */

/** 参数型异常 code */
const val PARAM_EXCEPTION_CODE = 1000

/** 路由 path 找不到 */
const val ROUTE_PATH_NOT_FOUND = PARAM_EXCEPTION_CODE + 100

/** 传入了错误的 [RouteTypes] */
const val ROUTE_TYPE_ERROR = PARAM_EXCEPTION_CODE + 101

/** YCR 参数异常 */
internal class YCRParameterException(
    /** 错误码 = [PARAM_EXCEPTION_CODE] + reason code */
    override val code: Int,
    val msg: String
) : InvalidParameterException(msg), IYCRExceptions

// ==========================================================================

/** 运行时异常的 code */
const val RUNTIME_EXCEPTION_CODE = 2000

/** 拦截器重复处理 */
const val INTERCEPTOR_REPEAT_PROCESS = RUNTIME_EXCEPTION_CODE + 100

/** 拦截器处理超时 */
const val INTERCEPTOR_TIME_OUT = RUNTIME_EXCEPTION_CODE + 101

/** YCR 运行时异常 */
internal class YCRRuntimeException(
    /** 错误码 = [RUNTIME_EXCEPTION_CODE] + reason code */
    override val code: Int,
    val msg: String
) : RuntimeException(msg), IYCRExceptions

// ==========================================================================

/** 外部异常的 code */
const val EXTERNAL_EXCEPTION_CODE = 9000

/** 外部原因导致的异常 */
open class YCRExternalException(
    cause: Throwable?,
    override val code: Int = EXTERNAL_EXCEPTION_CODE
) : Exception(cause), IYCRExceptions

// ==========================================================================

/** 用于标记是 YCR 的异常类型 */
interface IYCRExceptions {
    /** 错误码 */
    val code: Int
}

