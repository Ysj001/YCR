package com.ysj.lib.route.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner

import androidx.lifecycle.OnLifecycleEvent


/**
 * 路由用的，组件生命周期观察者
 *
 * @author Ysj
 * Create time: 2020/10/15
 */
interface RouteLifecycleObserver : LifecycleObserver {

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy(owner: LifecycleOwner)

}