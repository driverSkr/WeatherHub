package com.driverskr.weatherhub

import com.baidu.location.LocationClient
import com.driverskr.lib.BaseApplication

/**
 * @Author: driverSkr
 * @Time: 2023/11/24 17:47
 * @Description: $
 */
class MyApplication: BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        //使用定位需要同意隐私合规政策
        LocationClient.setAgreePrivacy(true)
    }
}