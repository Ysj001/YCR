package com.ysj.lib.ycr.callback

import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.exception.IYCRExceptions

/**
 * YCR 的异常回调
 *
 * @author Ysj
 * Create time: 2020/10/27
 */
interface YCRExceptionCallback {

    /**
     * 处理异常
     *
     * @return 返回 true 则不进行入全局处理
     */
    fun handleException(postman: Postman, e: IYCRExceptions): Boolean
}