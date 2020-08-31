// IRouteService.aidl
package com.ysj.lib.route.remote;

import com.ysj.lib.route.remote.RemoteParam;

interface IRouteService {

    /**
     * 注册路由组
     */
    void registerRouteGroup(String group, in RemoteParam param);
}
