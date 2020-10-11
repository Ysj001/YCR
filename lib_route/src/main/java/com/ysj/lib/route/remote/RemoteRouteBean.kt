package com.ysj.lib.route.remote

import android.os.Parcel
import android.os.Parcelable
import com.ysj.lib.route.annotation.RouteBean
import com.ysj.lib.route.annotation.RouteTypes
import com.ysj.lib.route.entity.Postman

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
                moduleId = "${parcel.readString()}"
                className = "${parcel.readString()}"
            }
            .withAll(parcel.readBundle())
            .withRequestCode(parcel.readInt())
            .withRouteAction(parcel.readString())
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(routeBean) {
            dest.also {
                it.writeString(group)
                it.writeString(path)
                it.writeString(types?.name)
                it.writeString(moduleId)
                it.writeString(className)
                if (this is Postman) {
                    it.writeBundle(bundle)
                    it.writeInt(requestCode)
                    it.writeString(actionName)
                } else {
                    it.writeBundle(null)
                    it.writeInt(-1)
                    it.writeString(null)
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