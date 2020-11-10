package com.ysj.lib.ycr.lifecycle

import android.app.Activity
import android.app.Fragment
import android.content.Intent

/**
 * 帮助获取 [Activity.onActivityResult]
 *
 * @author Ysj
 * Create time: 2020/10/19
 */
@Suppress("DEPRECATION")
internal class ActivityResultFragment : Fragment() {

    companion object {
        /** 添加到 [Activity] 的标记 */
        const val TAG = "TAG_FRAGMENT_ACTIVITY_RESULT"

        val onActivityResultMethod = Activity::class.java.getDeclaredMethod(
            "onActivityResult",
            Int::class.java,
            Int::class.java,
            Intent::class.java
        ).apply { isAccessible = true }
    }

    var listener: ((Int, Int, Intent?) -> Unit)? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activity?.also { onActivityResultMethod.invoke(it, requestCode, resultCode, data) }
        listener?.invoke(requestCode, resultCode, data)
    }
}