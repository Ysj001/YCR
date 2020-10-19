package com.ysj.lib.route.lifecycle

import android.app.Activity
import android.app.Fragment
import android.content.Intent

/**
 * 帮助获取 [Activity.onActivityResult]
 *
 * @author Ysj
 * Create time: 2020/10/19
 */
internal class ActivityResultFragment : Fragment() {

    companion object {
        /** 添加到 [Activity] 的标记 */
        const val TAG = "TAG_FRAGMENT_ACTIVITY_RESULT"
    }

    var listener: ((Int, Int, Intent?) -> Unit)? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activity?.also {
            val onActivityResult = it.javaClass.getDeclaredMethod(
                "onActivityResult",
                Int::class.java,
                Int::class.java,
                Intent::class.java
            )
            onActivityResult.isAccessible = true
            onActivityResult.invoke(it, requestCode, resultCode, data)
        }
        listener?.invoke(requestCode, resultCode, data)
    }
}