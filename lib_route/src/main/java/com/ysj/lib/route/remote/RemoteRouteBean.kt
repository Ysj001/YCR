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

    constructor(parcel: Parcel) : this(RouteBean().apply {
        parcel.also {
            group = "${it.readString()}"
            path = "${it.readString()}"
            types = RouteTypes("${it.readString()}")
            moduleId = "${it.readString()}"
            className = "${it.readString()}"
            if (this is Postman) {
                withAll(it.readBundle())
                withRequestCode(it.readInt())
                withRouteAction(it.readString())
            }
        }
    })

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