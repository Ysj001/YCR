package com.ysj.lib.route.module.m1

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.base.utils.ToastUtil
import com.ysj.lib.route.YCR
import com.ysj.lib.route.annotation.Route
import com.ysj.lib.route.entity.ActivityResult

@Route("/m1/HomeActivity")
class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_m1_activity_home)
    }

    fun onUserCenterClicked(view: View) {
        YCR.getInstance()
            .build("/m1/UserCenterActivity")
            .doOnInterrupt { postman, reason ->
                if (reason.code != LoginInterceptor.INTERRUPT_CODE_NOT_LOGIN) return@doOnInterrupt
                YCR.getInstance()
                    .build("/java/LoginActivity")
                    .addOnResultCallback { result: ActivityResult? ->
                        if (result == null) return@addOnResultCallback
                        if (result.resultCode == RESULT_OK) {
                            ToastUtil.showShortToast("登录成功")
                            YCR.getInstance().navigation(postman)
                        }
                    }
                    .navigationSync(this)
            }
            .doOnException { postman, e ->
                e.printStackTrace()
                false
            }
            .navigation(this)
    }

}