package com.ysj.lib.ycr.remote

import android.os.Parcel
import android.os.Parcelable
import com.ysj.lib.ycr.annotation.RouteBean
import com.ysj.lib.ycr.annotation.RouteTypes
import com.ysj.lib.ycr.entity.Postman

/**
 * 包装路由对象用于跨进程
 *
 * @author Ysj
 * Create time: 2020/8/23
 */
class RemoteRouteBean(val routeBean: RouteBean) : Parcelable {

    constructor(parcel: Parcel) : this(
        Postman(parcel.readString() ?: "", parcel.readString() ?: "")
            .apply {
                types = RouteTypes("${parcel.readString()}")
                applicationId = "${parcel.readString()}"
                className = "${parcel.readString()}"
            }
            .withBundle(parcel.readBundle())
            .withRequestCode(parcel.readInt())
            .withRouteAction(parcel.readString())
            .withFlags(parcel.readInt())
            .apply { if (parcel.readInt() == 1) useGreenChannel() }
            .apply { if (parcel.readInt() == 1) isDestroy = true }
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(routeBean) {
            dest.also {
                it.writeString(group)
                it.writeString(path)
                it.writeString(types?.name)
                it.writeString(applicationId)
                it.writeString(className)
                if (this is Postman) {
                    it.writeBundle(bundle)
                    it.writeInt(requestCode)
                    it.writeString(actionName)
                    it.writeInt(this.flags)
                    it.writeInt(if (greenChannel) 1 else 0)
                    it.writeInt(if (isDestroy) 1 else 0)
                } else {
                    it.writeBundle(null)
                    it.writeInt(-1)
                    it.writeString(null)
                    it.writeInt(0)
                    it.writeInt(0)
                    it.writeInt(0)
                }
            }
        }
    }

    override fun toString() = "\n ($routeBean)"
    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<RemoteRouteBean> {
        override fun createFromParcel(parcel: Parcel): RemoteRouteBean {
            return RemoteRouteBean(parcel)
        }

        override fun newArray(size: Int): Array<RemoteRouteBean?> {
            return arrayOfNulls(size)
        }
    }
}