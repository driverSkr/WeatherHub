package com.driverskr.weatherhub.location

import com.baidu.location.BDLocation

/**
 * @Author: driverSkr
 * @Time: 2023/11/24 16:52
 * @Description: 定位接口$
 */
interface LocationCallback {

    /**
     * 接收定位
     * @param bdLocation 定位数据
     */
    fun onReceiveLocation(bdLocation: BDLocation)
}