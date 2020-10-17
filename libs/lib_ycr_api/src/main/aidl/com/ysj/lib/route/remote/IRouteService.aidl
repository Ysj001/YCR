// IRouteService.aidl
package com.ysj.lib.route.remote;

import com.ysj.lib.route.remote.RemoteParam;
import com.ysj.lib.route.remote.RemoteRouteBean;
import com.ysj.lib.route.remote.RemoteInterceptorCallback;

interface IRouteService {

    /**
     * 注册 application id
     */
    void registerApplicationId(String applicationId);

    /**
     * 获取所有组件的 application id
     */
    RemoteParam getAllApplicationId();

    /**
     * 注册路由组
     */
    void registerRouteGroup(String group, in RemoteParam param);

    /**
     * 查找路由
     */
    RemoteRouteBean findRouteBean(String group, String path);

    /**
     * 执行行为
     */
    RemoteParam doAction(String className, String actionName);

    /**
     * 处理拦截器
     */
    void handleInterceptor(long timeoout, in RemoteRouteBean routeBean, in RemoteInterceptorCallback callback);
}
