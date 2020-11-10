package com.ysj.lib.route.module.m1

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.base.utils.ToastUtil
import com.ysj.lib.ycr.YCR
import com.ysj.lib.ycr.annotation.Route
import com.ysj.lib.ycr.entity.ActivityResult

@Route("/m1/HomeActivity")
class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_m1_activity_home)
    }

    fun onUserCenterClicked(view: View) {
        // 演示拦截器的使用
        YCR.getInstance()
            .build("/m1/UserCenterActivity")
            .doOnInterrupt { postman, reason ->
                if (reason.code != LoginInterceptor.INTERRUPT_CODE_NOT_LOGIN) return@doOnInterrupt
                YCR.getInstance()
                    .build("/java/LoginActivity")
                    .withRequestCode(1)
                    .addOnResultCallback { result: ActivityResult? ->
                        // 演示代替 activity 的 onActivityResult
                        if (result == null) return@addOnResultCallback
                        if (result.requestCode == 1 && result.resultCode == RESULT_OK) {
                            ToastUtil.showShortToast("登录成功")
                            YCR.getInstance().navigation(postman)
                        }
                    }
                    .navigationSync(this)
            }
            .doOnException { _, e ->
                e.printStackTrace()
                false
            }
            .navigation(this)
    }

}