// IRouteService.aidl
package com.ysj.lib.route.remote;

import com.ysj.lib.route.remote.RemoteParam;
import com.ysj.lib.route.remote.RemoteRouteBean;
import com.ysj.lib.route.remote.RemoteInterceptorCallback;

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
     * 查找匹配的拦截器
     */
    RemoteParam findInterceptor(in RemoteRouteBean routeBean);

    /**
     * 处理拦截器
     */
    void handleInterceptor(in RemoteParam param, in RemoteInterceptorCallback callback);
}
