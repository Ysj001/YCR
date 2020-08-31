package com.ysj.lib.route.remote

import android.os.Parcel
import android.os.Parcelable

/**
 * 用于跨进程传递参数
 *
 * @author Ysj
 * Create time: 2020/8/21
 */
class RemoteParam() : Parcelable {

    var params: HashMap<String, Parcelable> = HashMap()
        private set

    constructor(parcel: Parcel) : this() {
        parcel.readMap(params as Map<String, Parcelable>, javaClass.classLoader)
    }

    override fun toString() = "\n($params)"
    override fun writeToParcel(dest: Parcel, flags: Int) = dest.writeMap(params as Map<*, *>)
    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<RemoteParam> {
        override fun createFromParcel(parcel: Parcel): RemoteParam {
            return RemoteParam(parcel)
        }

        override fun newArray(size: Int): Array<RemoteParam?> {
            return arrayOfNulls(size)
        }
    }

}