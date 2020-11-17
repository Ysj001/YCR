package com.ysj.lib.router

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.ycr.YCR

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun gotoHomePage(view: View) {
        YCR.getInstance()
            .build("/m1/HomeActivity")
            .navigation(this)
    }

}