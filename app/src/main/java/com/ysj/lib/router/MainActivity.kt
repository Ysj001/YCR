package com.ysj.lib.router

import android.os.Bundle
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
        tv.setOnClickListener {
            Router.getInstance()
                .build("/m1/MainActivity")
                .navigation(this)
        }
    }

}