// IRouteService.aidl
package com.ysj.lib.ycr.remote;

import com.ysj.lib.ycr.remote.RemoteParam;
import com.ysj.lib.ycr.remote.RemoteRouteBean;
import com.ysj.lib.ycr.remote.RemoteInterceptorCallback;

interface IRouteService {

    /**
     * 注册到主组件 App
     */
    void registerToMainApp(String applicationId);

    /**
     * 获取所有组件的 application id
     */
    RemoteParam getAllApplicationId();

    /**
     * 注册路由组到主组件
     */
    void registerRouteGroup(String group, in RemoteParam param);

    /**
     * 查找路由
     */
    RemoteRouteBean findRouteBean(String group, String path);

    /**
     * 执行行为
     */
    RemoteParam doAction(in RemoteRouteBean routeBean);

    /**
     * 获取该进程所有的拦截器
     */
    RemoteParam getAllInterceptors();

    /**
     * 处理拦截器
     */
    void handleInterceptor(in RemoteParam param, in RemoteInterceptorCallback callback);

    /**
     * 获取该进程所有的全局异常处理器
     */
    RemoteParam getAllGlobalExceptionProcessors();

    /**
     * 处理全局异常处理器
     */
    boolean handleExceptionProcessor(in RemoteParam param);
}
