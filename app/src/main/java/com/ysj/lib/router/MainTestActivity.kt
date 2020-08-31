package com.ysj.lib.router

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.route.annotation.Route
import kotlinx.android.synthetic.main.activity_main.*

@Route("/app/MainTestActivity")
class MainTestActivity : AppCompatActivity() {

    private val TAG = "MainTestActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_test)
        tv.setOnClickListener {
            Log.i(TAG, "onCreate")
        }
    }

}