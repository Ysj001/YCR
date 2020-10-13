// RemoteInterceptorCallback.aidl
package com.ysj.lib.route.remote;

import com.ysj.lib.route.remote.RemoteRouteBean;
import com.ysj.lib.route.remote.RemoteParam;

interface RemoteInterceptorCallback {

    void onContinue(in RemoteRouteBean routeBean);

    void onInterrupt(in RemoteParam param);
}
