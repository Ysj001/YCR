package com.ysj.lib.route.module.m1

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.ysj.lib.route.annotation.Route
import com.ysj.lib.route.template.IActionProcessor

/**
 *
 *
 * @author Ysj
 * Create time: 2020/9/26
 */
@Route("/m1/actions")
class M1ActionProcessor() : IActionProcessor, Parcelable {

    private val TAG = "M1Actions"

    constructor(parcel: Parcel) : this()

    @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
    override fun <T> doAction(actionName: String): T {
        return when (actionName) {
            "m1_test" -> {
                Log.i(TAG, "doAction: m1_test")
            }
            else -> Unit
        } as T
    }

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel?, flags: Int) = Unit

    companion object CREATOR : Parcelable.Creator<M1ActionProcessor> {
        override fun createFromParcel(parcel: Parcel): M1ActionProcessor {
            return M1ActionProcessor(parcel)
        }

        override fun newArray(size: Int): Array<M1ActionProcessor?> {
            return arrayOfNulls(size)
        }
    }
}