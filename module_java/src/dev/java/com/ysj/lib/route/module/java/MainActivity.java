package com.ysj.lib.route.module.java;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ysj.lib.route.YCR;
import com.ysj.lib.route.annotation.Route;
import com.ysj.lib.route.callback.RouteResultCallback;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.module_java_activity_main);
    }

}
