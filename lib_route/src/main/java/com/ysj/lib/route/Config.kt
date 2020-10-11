package com.ysj.lib.route

/*
 * 路由框架的一些基础配置
 *
 * @author Ysj
 * Create time: 2020/10/11
 */

/** 拦截器超时时间，ms（主线程） */
const val INTERCEPTOR_TIME_OUT_MAIN_TH = 2_000L

/** 拦截器超时时间，ms（子线程） */
const val INTERCEPTOR_TIME_OUT_SUB_TH = 10_000L