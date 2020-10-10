package com.ysj.lib.router;

import androidx.annotation.Nullable;

import com.ysj.lib.route.Router;
import com.ysj.lib.route.callback.RouteResultCallback;


import kotlin.Unit;

/**
 * <p>
 *
 * @author Ysj
 * Create time: 2020/10/6
 */
public class T {
    void a() {
        Router.getInstance()
                .build("")
                .withInt("a",1)
                .doOnContinue(postman -> {})
                .doOnResult((RouteResultCallback<String>) result -> {

                });
    }
}
