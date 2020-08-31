package com.ysj.lib.route.module.m1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.route.Router
import com.ysj.lib.route.annotation.Route
import com.ysj.route.generated.routes.`Route$$Path$$m1`
import kotlinx.android.synthetic.main.activity_main.*

@Route("/m1/MainActivity")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // TODO：自动注册
        Router.getInstance().registerRouteGroup(`Route$$Path$$m1`())
        tv.setOnClickListener { }
    }

}