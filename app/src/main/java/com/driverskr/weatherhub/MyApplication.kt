package com.driverskr.weatherhub

import android.app.Application
import com.baidu.location.LocationClient

/**
 * @Author: driverSkr
 * @Time: 2023/11/24 17:47
 * @Description: $
 */
class MyApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        //使用定位需要同意隐私合规政策
        LocationClient.setAgreePrivacy(true)
    }
}