package com.ysj.lib.route.module.m1

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ysj.lib.base.mock.LoginInterceptor
import com.ysj.lib.base.mock.MockUserLogin
import com.ysj.lib.base.utils.ToastUtil
import com.ysj.lib.ycr.YCR
import com.ysj.lib.ycr.annotation.Route
import com.ysj.lib.ycr.callback.InterceptorCallback
import com.ysj.lib.ycr.entity.ActivityResult
import com.ysj.lib.ycr.entity.InterruptReason
import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.template.IInterceptor

@Route("/m1/HomeActivity")
class HomeActivity : AppCompatActivity() {

    private var skipLocalInterceptor = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.module_m1_activity_home)
    }

    fun interceptorDemo(view: View) {
        // 演示拦截器的使用
        YCR.getInstance()
            .build("/m1/UserCenterActivity")
            .withInterceptor(object : IInterceptor {
                // 演示局部拦截器
                override fun onIntercept(postman: Postman, callback: InterceptorCallback) {
                    if (skipLocalInterceptor) callback.onContinue(postman)
                    else {
                        callback.onInterrupt(
                            postman,
                            InterruptReason<Any>(111, "我是局部拦截器，再点一次跳过")
                        )
                        skipLocalInterceptor = true
                    }
                }
            })
            .doOnInterrupt { postman, reason ->
                ToastUtil.showShortToast(reason.msg)
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
            .navigation(this)
    }

    fun injectParamDemo(view: View) {
        // 路由参数注入演示
        YCR.getInstance()
            .build("/java/InjectParamActivity")
            .withInt("i", 100)
            .withBoolean("b", true)
            .withString("str", "test")
            .withSerializable("userInfo", MockUserLogin.UserInfo("inject test", 20))
            .apply { withBundle("bd", Bundle(bundle)) }
            .navigation(this)
    }

    fun transitionDemo(view: View) {
        // 演示设置转场效果
        YCR.getInstance()
            .build("/m1/HomeActivity")
            .withTransition(R.anim.module_m1_entry, R.anim.module_m1_exit)
            .doOnFinished(::finish)
            .navigation(this)
    }

}