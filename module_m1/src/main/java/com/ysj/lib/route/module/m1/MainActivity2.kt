package com.ysj.lib.route.module.m1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.route.annotation.Route

@Route("/m2/MainActivity2")
class MainActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_m1_activity_main)
    }

}