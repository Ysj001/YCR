package com.ysj.lib.ycr.entity

import android.app.Activity
import android.content.Intent

/**
 * 封装 [Activity] 的返回结果
 *
 * @author Ysj
 * Create time: 2020/10/19
 */
class ActivityResult(
    /**
     * The integer request code originally supplied to startActivityForResult(),
     * allowing you to identify who this result came from.
     */
    val requestCode: Int,
    /**
     * The integer result code returned by the child activity
     */
    val resultCode: Int,
    /**
     * An Intent, which can return result data to the caller，
     * (various data can be attached to Intent "extras").
     */
    val data: Intent?
)