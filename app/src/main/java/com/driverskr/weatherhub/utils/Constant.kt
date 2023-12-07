package com.driverskr.weatherhub.utils

import androidx.preference.PreferenceManager
import com.driverskr.lib.BaseApplication.Companion.context

/**
 * @Author: driverSkr
 * @Time: 2023/11/20 15:18
 * @Description: $
 */
object Constant {

    const val BAIDU_KEY = "CZkxHqminzGKQuvTNpnuqX9oYfjV57B0"

    const val HEFENG_KEY = "809025b8d5864149af26f51637d89049"

    //应用设置里的温度格式
    @JvmField
    var APP_SETTING_UNIT = PreferenceManager.getDefaultSharedPreferences(context).getString("unit","she")
    //前台服务是否开启
    val FOREGROUND_CHECKBOX get() = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("foreground_checkout",false)

    @JvmField
    @Volatile
    var CITY_CHANGE = false

    @JvmField
    var visibleHeight = 0

    @JvmField
    var screenHeight = 0

    const val TC_APP_ID = "101991873"

    /**
     * 获取当前应用程序的名称。
     * @return 当前应用程序的名称。
     */
    val appName: String get() = context.resources.getString(context.applicationInfo.labelRes)
}