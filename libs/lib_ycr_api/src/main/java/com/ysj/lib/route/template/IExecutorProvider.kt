package com.ysj.lib.route.template

import java.util.concurrent.ThreadPoolExecutor

/**
 * 用于给 YCR 提供 [ThreadPoolExecutor]
 *
 * @author Ysj
 * Create time: 2020/11/8
 */
interface IExecutorProvider : YCRTemplate {

    fun providerExecutor(): ThreadPoolExecutor?
}