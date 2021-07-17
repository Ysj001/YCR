package com.ysj.lib.router

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.base.YCRConst
import com.ysj.lib.ycr.YCR

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun gotoHomePage(view: View) {
        YCR.getInstance()
            .build(YCRConst.route.m1_HomeActivity)
            .navigation(this)
    }

}