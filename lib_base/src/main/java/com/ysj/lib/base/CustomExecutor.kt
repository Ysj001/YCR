package com.ysj.lib.base

import com.ysj.lib.base.utils.Run
import com.ysj.lib.ycr.YCR
import com.ysj.lib.ycr.template.IExecutorProvider
import java.util.concurrent.ThreadPoolExecutor

/**
 * 演示给 [YCR] 提供自定义的 [ThreadPoolExecutor]
 * 会在调用 [YCR.getCustomExecutor] 时构造该类
 * 因此该类必须有公有无参构造器
 *
 * @author Ysj
 * Create time: 2020/11/8
 */
class CustomExecutor : IExecutorProvider {

    override fun providerExecutor(): ThreadPoolExecutor? = Run.getWorker() as ThreadPoolExecutor?
}

// 演示有两个 IExecutorProvider 则在编译时会报错
//class CustomExecutor2 : IExecutorProvider {
//
//    override fun providerExecutor(): ThreadPoolExecutor? = Run.getWorker() as ThreadPoolExecutor?
//}