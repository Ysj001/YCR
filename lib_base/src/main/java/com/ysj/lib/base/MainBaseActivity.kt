package com.ysj.lib.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.route.annotation.Route

@Route("/base/MainBaseActivity")
class MainBaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_base)
    }
}