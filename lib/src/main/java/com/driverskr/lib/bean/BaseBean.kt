package com.driverskr.lib.bean

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 17:23
 * @Description: Json对象基类$
 */
open class BaseBean<T> {
    var code = -1
    var msg = ""
    var result: T? = null
}