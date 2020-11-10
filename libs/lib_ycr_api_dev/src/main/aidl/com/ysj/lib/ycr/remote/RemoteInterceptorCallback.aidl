// RemoteInterceptorCallback.aidl
package com.ysj.lib.ycr.remote;

import com.ysj.lib.ycr.remote.RemoteRouteBean;
import com.ysj.lib.ycr.remote.RemoteParam;

interface RemoteInterceptorCallback {

    void onContinue(in RemoteRouteBean routeBean);

    void onInterrupt(in RemoteParam param);
}
