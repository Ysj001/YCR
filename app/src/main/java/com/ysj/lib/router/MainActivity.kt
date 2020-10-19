package com.ysj.lib.router

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.route.YCR
import com.ysj.lib.route.annotation.Route
import com.ysj.lib.route.callback.ActivityResult
import com.ysj.lib.route.callback.RouteResultCallback
import kotlinx.android.synthetic.main.activity_main.*

@Route("/app/MainActivity")
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "onActivityResult: $requestCode , $resultCode")
    }

    fun onClick(view: View) {
        when (view) {
            btnToM1App -> {
                YCR.getInstance()
                    .build("/m1/MainActivity")
                    .navigation(this)
            }
            btnDoM1Action -> {
                YCR.getInstance()
                    .build("/m1/actions")
                    .bindLifecycle(lifecycle)
                    .withRouteAction("m1_test_action1")
                    .addOnResultCallback(object : RouteResultCallback<Int?>() {
                        override fun onResult(result: Int?) {
                            Log.i(TAG, "doOnResult 1: $result")
                        }
                    })
                    .addOnResultCallback { result: Any? -> Log.i(TAG, "doOnResult 2: $result") }
                    .addOnResultCallback { result: Int? -> Log.i(TAG, "doOnResult 3: $result") }
                    .addOnResultCallback { result: String? -> Log.i(TAG, "doOnResult 4: $result") }
                    .navigation(this)
            }
            btnToJavaApp -> {
                YCR.getInstance()
                    .build("/java/MainActivity")
                    .bindLifecycle(lifecycle)
                    .withRequestCode(99)
                    .addOnResultCallback { result: ActivityResult? ->
                        Log.i(TAG, "ActivityResult: ${result?.requestCode} , ${result?.resultCode}")
                    }
                    .navigation(this)
            }
        }
    }

}