package com.ysj.lib.router

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.route.Router
import com.ysj.lib.route.annotation.Route
import kotlinx.android.synthetic.main.activity_main.*

@Route("/app/MainActivity")
class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(view: View) {
        when (view) {
            btnToM1App -> {
                Router.getInstance()
                    .build("/m1/MainActivity")
                    .navigation(this)
            }
            btnDoM1Action -> {
                Router.getInstance()
                    .build("/m1/actions")
                    .withRouteAction("m1_test_action1")
                    .doOnResult { result: String? -> Log.i(TAG, "onClick: $result") }
                    .navigation(this)
            }
        }
    }

}