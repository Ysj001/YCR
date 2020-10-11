// RemoteInterceptorCallback.aidl
package com.ysj.lib.route.remote;

import com.ysj.lib.route.remote.RemoteParam;

interface RemoteInterceptorCallback {

    void onContinue();

    void onInterrupt(in RemoteParam param);
}
