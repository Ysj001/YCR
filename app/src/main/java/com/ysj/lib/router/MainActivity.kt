package com.ysj.lib.router

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.base.utils.ToastUtil
import com.ysj.lib.route.YCR

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun gotoHomePage(view: View) {
        YCR.getInstance()
            .build("/m1/HomeActivity")
            .doOnException { _, e ->
                ToastUtil.showShortToast(e.message)
                false
            }
            .navigation(this)
    }

}