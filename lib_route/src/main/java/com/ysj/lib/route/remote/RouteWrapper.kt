package com.ysj.lib.route.remote

import android.os.Parcel
import android.os.Parcelable
import com.ysj.lib.route.annotation.RouteBean
import com.ysj.lib.route.annotation.RouteTypes

/**
 * 包装路由对象用于跨进程
 *
 * @author Ysj
 * Create time: 2020/8/23
 */
class RouteWrapper(val routeBean: RouteBean) : Parcelable {

    constructor(parcel: Parcel) : this(RouteBean().apply {
        with(parcel) {
            group = "${readString()}"
            path = "${readString()}"
            types = RouteTypes("${readString()}")
            moduleId = "${readString()}"
            className = "${readString()}"
        }
    })

    override fun writeToParcel(dest: Parcel, flags: Int) {
        with(routeBean) {
            with(dest) {
                writeString(group)
                writeString(path)
                writeString(types?.name)
                writeString(moduleId)
                writeString(className)
            }
        }
    }

    override fun toString() = "\n ($routeBean)"
    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<RouteWrapper> {
        override fun createFromParcel(parcel: Parcel): RouteWrapper {
            return RouteWrapper(parcel)
        }

        override fun newArray(size: Int): Array<RouteWrapper?> {
            return arrayOfNulls(size)
        }
    }
}