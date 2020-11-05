package com.ysj.lib.router

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.route.YCR
import com.ysj.lib.route.annotation.Route
import com.ysj.lib.route.callback.RouteResultCallback
import com.ysj.lib.route.entity.ActivityResult
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
//                    .build("/m1/MainActivity")
                    .build("/base/MainBaseActivity")
                    .navigation(this)
            }
            btnDoM1ActionAsync -> {
                YCR.getInstance()
//                    .build("/m1/actions")
                    .build("/base/actions")
                    .bindLifecycle(lifecycle)
                    .withRouteAction("m1_test_action1")
                    .addOnResultCallback(object : RouteResultCallback<Int?>() {
                        override fun onResult(result: Int?) {
                            Log.i(TAG, "doOnResult 1: $result")
                            throw Exception("test exception")
                        }
                    })
                    .addOnResultCallback { result: Any? -> Log.i(TAG, "doOnResult 2: $result") }
                    .addOnResultCallback { result: Int? -> Log.i(TAG, "doOnResult 3: $result") }
                    .addOnResultCallback { result: String? -> Log.i(TAG, "doOnResult 4: $result") }
                    .doOnInterrupt { postman, reason ->
                        Log.i(TAG, "doOnInterrupt ${reason.code}")
                    }
                    .doOnException { p, e ->
                        e.printStackTrace()
                        false
                    }
                    .navigation(this)
            }
            btnDoM1ActionSync -> {
                val result = YCR.getInstance()
                    .build("/m1/actions")
                    .withRouteAction("m1_test_action1")
                    .navigationSync(this)
                Log.i(TAG, "btnDoM1ActionSync: $result")
            }
            btnToJavaApp -> {
                YCR.getInstance()
                    .build("/app/MainTestActivity")
                    .bindLifecycle(lifecycle)
                    .withRequestCode(99)
                    .addOnResultCallback { result: ActivityResult? ->
                        Log.i(TAG, "ActivityResult: ${result?.requestCode} , ${result?.resultCode}")
                    }
                    .doOnException { postman, e ->
                        e.printStackTrace()
                        false
                    }
                    .navigation(this)
            }
        }
    }

}