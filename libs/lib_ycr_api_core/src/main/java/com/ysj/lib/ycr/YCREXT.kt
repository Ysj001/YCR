package com.ysj.lib.ycr

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.util.Log
import com.ysj.lib.ycr.annotation.SUFFIX_ROUTE_PARAM
import com.ysj.lib.ycr.entity.ActivityResult
import com.ysj.lib.ycr.entity.Postman
import com.ysj.lib.ycr.exception.YCRExceptionFactory
import com.ysj.lib.ycr.lifecycle.ActivityResultFragment
import com.ysj.lib.ycr.template.IProviderParam
import com.ysj.lib.ycr.template.YCRTemplate

/*
 * YCR 扩展
 *
 * @author Ysj
 * Create time: 2020/11/28
 */

internal fun YCR.inject(obj: Any, parent: Class<*>?) {
    val clazz: Class<*> = parent ?: obj.javaClass
    val className = clazz.name + SUFFIX_ROUTE_PARAM
    val injector = Caches.paramCache[className]
        ?: getTemplateInstance<IProviderParam>(className)
            ?.also { Caches.paramCache[className] = it }
        ?: return
    injector.injectParam(obj)
    // 注入父类
    val superclass = clazz.superclass ?: return
    if (!Activity::class.java.isAssignableFrom(superclass)) return
    inject(obj, superclass)
}

@Suppress("DEPRECATION")
internal fun YCR.handleRouteActivity(postman: Postman, resultCallback: (Any?) -> Unit) =
    runOnMainThread {
        val context = postman.getContext() ?: return@runOnMainThread
        val intent = Intent()
            .addFlags(postman.flags)
            .putExtras(postman.bundle)
            .setComponent(ComponentName(postman.applicationId, postman.className))
        try {
            if (context !is Activity) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent, postman.optionsCompat)
                return@runOnMainThread
            }
            if (postman.routeResultCallbacks == null || postman.requestCode < 0) {
                context.startActivityForResult(
                    intent,
                    postman.requestCode,
                    postman.optionsCompat
                )
                if (postman.enterAnim != -1 && postman.exitAnim != -1) context.overridePendingTransition(
                    postman.enterAnim,
                    postman.exitAnim
                )
                return@runOnMainThread
            }
            val sfm = context.fragmentManager
            var fragment = sfm.findFragmentByTag(ActivityResultFragment.TAG)
            if (fragment === null) {
                fragment = ActivityResultFragment()
                fragment.listener = { rqc: Int, rsc: Int, data: Intent? ->
                    resultCallback(ActivityResult(rqc, rsc, data))
                }
                sfm.beginTransaction().add(fragment, ActivityResultFragment.TAG)
                    .commitAllowingStateLoss()
                sfm.executePendingTransactions()
            }
            fragment.startActivityForResult(
                intent,
                postman.requestCode,
                postman.optionsCompat
            )
            if (postman.enterAnim != -1 && postman.exitAnim != -1) context.overridePendingTransition(
                postman.enterAnim,
                postman.exitAnim
            )
        } catch (e: Exception) {
            callException(
                postman,
                YCRExceptionFactory.navigationException(e)
            )
        }
    }

internal fun <T : YCRTemplate> getTemplateInstance(className: String): T? {
    try {
        @Suppress("UNCHECKED_CAST")
        return Class.forName(className).getConstructor().newInstance() as T
    } catch (e: Exception) {
        Log.d("YCR-DEV", "$className 没有在该进程找到 --> ${e.message}")
    }
    return null
}