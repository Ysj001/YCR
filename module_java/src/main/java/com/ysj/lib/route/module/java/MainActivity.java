package com.ysj.lib.route.module.java;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ysj.lib.route.YCR;
import com.ysj.lib.route.annotation.Route;
import com.ysj.lib.route.callback.InterceptorCallback;
import com.ysj.lib.route.callback.RouteResultCallback;
import com.ysj.lib.route.entity.InterruptReason;
import com.ysj.lib.route.entity.Postman;

import org.jetbrains.annotations.NotNull;

@Route(path = "/java/MainActivity")
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setResult(111);
        setContentView(R.layout.module_java_activity_main);
        findViewById(R.id.btnM1Action1).setOnClickListener(v ->
                YCR.getInstance()
                        .build("/m1/actions")
                        .withRouteAction("m1_test_action1")
                        .addOnResultCallback(new RouteResultCallback<String>() {
                            @Override
                            public void onResult(@Nullable String result) {
                                Log.i(TAG, "onResult 1: " + result);
                            }
                        })
                        .addOnResultCallback(new RouteResultCallback<Integer>() {
                            @Override
                            public void onResult(@Nullable Integer result) {
                                Log.i(TAG, "onResult 2: " + result);
                            }
                        })
                        .navigation(this));
    }

}
