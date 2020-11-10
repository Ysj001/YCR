package com.ysj.lib.ycr.annotation

import javax.lang.model.element.TypeElement

/**
 * 路由的注解实体
 *
 * @author Ysj
 * Create time: 2020/8/4
 */
open class RouteBean {

    /** 路由的组 */
    var group: String = ""

    /** 路由的路径 */
    var path: String = ""

    /** 该路由作用的类型 */
    var types: RouteTypes? = null

    /** 作用的类元素，实际类型为 [TypeElement] 为方便跨进程传输定义为 [Any] */
    var typeElement: Any? = null

    /** 作用的组件的 applicationId */
    var applicationId: String = ""

    /** 作用的 Class 的全限定名 */
    var className: String = ""

    constructor()

    constructor(group: String, path: String) : this() {
        this.group = group
        this.path = path
    }

    override fun toString(): String {
        return """
            RouteBean(
                group=$group
                path=$path 
                types=$types
                typeElement=$typeElement
                applicationId=$applicationId
                className=$className
            )
        """.trimIndent()
    }


}