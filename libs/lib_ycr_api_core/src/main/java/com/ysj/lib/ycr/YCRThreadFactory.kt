package com.ysj.lib.ycr

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * YCR 的线程池工厂
 *
 * @author Ysj
 * Create time: 2020/11/7
 */
internal class YCRThreadFactory(poolName: String) : ThreadFactory {

    companion object {
        private val poolNumber = AtomicInteger(1)
    }

    private val group: ThreadGroup
    private val threadNumber = AtomicInteger(1)
    val namePrefix: String

    init {
        val s = System.getSecurityManager()
        group = if (s != null) s.threadGroup else Thread.currentThread().threadGroup!!
        namePrefix = "YCR-${poolName}-pool-${poolNumber.getAndIncrement()}-thread-"
    }

    override fun newThread(r: Runnable) =
        Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0).apply {
            if (isDaemon) isDaemon = false
            if (priority != Thread.NORM_PRIORITY) priority = Thread.NORM_PRIORITY
        }

}