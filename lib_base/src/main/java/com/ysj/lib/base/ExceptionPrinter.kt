package com.ysj.lib.base

import android.util.Log
import com.ysj.lib.base.utils.ToastUtil
import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.exception.IYCRExceptions
import com.ysj.lib.ycr.template.IGlobalExceptionProcessor

/**
 *
 *
 * @author Ysj
 * Create time: 2020/11/17
 */
class ExceptionPrinter : IGlobalExceptionProcessor {

    private val TAG = "ExceptionPrinter"

    override fun handleException(postman: Postman, e: IYCRExceptions): Boolean {
        Log.w(TAG, "", e as Throwable)
        ToastUtil.showShortToast(e.message)
        return false
    }

}