package com.ysj.lib.base.utils;


import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 用于线程切换的工具类
 * <p>
 *
 * @author YSJ
 */
public class Run {

    // 主线程 Handler
    private static volatile Handler mainHandler;
    // 工作线程池
    private static volatile ExecutorService worker;

    /**
     * @return 主线程的 Handler
     */
    public static Handler getMainHandler() {
        if (mainHandler == null) {
            synchronized (Run.class) {
                if (mainHandler == null) {
                    mainHandler = new Handler(Looper.getMainLooper());
                }
            }
        }
        return mainHandler;
    }

    /**
     * @return 多线程的线程池
     */
    public static ExecutorService getWorker() {
        if (worker == null) {
            synchronized (Run.class) {
                if (worker == null) {
                    worker = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                }
            }
        }
        return worker;
    }

    /**
     * 将 Runnable 运行在 UI 线程
     *
     * @param runnable 要运行在 UI 线程的 Runnable
     */
    public static void runOnUiThread(Runnable runnable) {
        runOnUiThread(runnable, 0);
    }

    /**
     * 将 Runnable 运行在 UI 线程，可设置延迟时间
     *
     * @param runnable 要运行在 UI 线程的 Runnable
     */
    public static void runOnUiThread(Runnable runnable, long delayed) {
        // 判断当前是否是 UI 线程
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            getMainHandler().postDelayed(runnable, delayed);
        } else {
            runnable.run();
        }
    }

    public static void runOnBackground(Runnable runnable) {
        getWorker().execute(runnable);
    }
}
