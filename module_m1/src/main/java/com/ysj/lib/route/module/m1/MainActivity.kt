package com.ysj.lib.route.module.m1

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.route.YCR
import com.ysj.lib.route.annotation.Route
import kotlinx.android.synthetic.main.activity_main.*

@Route("/m1/MainActivity")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(view: View) {
        when (view) {
            btnToMainApp -> {
                YCR.getInstance()
                    .build("/app/MainActivity")
                    .navigation(this)
            }
            btnDoAppAction -> {
                YCR.getInstance()
                    .build("/app/actions")
                    .withRouteAction("app_test_action")
                    .navigation(this)
            }
        }
    }

}