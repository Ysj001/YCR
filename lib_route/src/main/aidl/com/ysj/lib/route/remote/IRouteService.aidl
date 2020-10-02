// IRouteService.aidl
package com.ysj.lib.route.remote;

import com.ysj.lib.route.remote.RemoteParam;
import com.ysj.lib.route.remote.RouteWrapper;

interface IRouteService {

    /**
     * 注册路由组
     */
    void registerRouteGroup(String group, in RemoteParam param);

    /**
     * 查找路由
     */
    RouteWrapper findRouteBean(String group, String path);

    /**
     * 执行行为
     */
    RemoteParam doAction(String className, String actionName);
}
