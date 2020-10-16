package com.ysj.lib.route.type

/*
 * 类型工具
 *
 * @author Ysj
 * Create time: 2020/10/15
 */

/**
 * 校验方法参数类型
 *
 * @param src 要校验的方法所在的 class
 * @param mdName 要校验的方法名
 * @param type 目标类型
 */
internal fun checkMethodParameterType(src: Class<*>, mdName: String, type: Class<*>?) =
    src.methods.filter { it.name == mdName }.let { mds ->
        var mpt: Class<*> = mds[0].parameterTypes[0]
        if (mds.size > 1 && mpt == Any::class.java) mpt = mds[1].parameterTypes[0]
        type?.let {
            var result = mpt.isAssignableFrom(it)
            if (!result) result = when (it.name) {
                "byte" -> java.lang.Byte::class.java === mpt
                "char" -> Character::class.java === mpt
                "int" -> Integer::class.java === mpt
                "long" -> java.lang.Long::class.java === mpt
                "float" -> java.lang.Float::class.java === mpt
                "double" -> java.lang.Double::class.java === mpt
                else -> false
            }
            result
        } ?: false
    }