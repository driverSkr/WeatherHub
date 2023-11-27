package com.driverskr.weatherhub.location

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.driverskr.lib.extension.logE
import java.lang.Exception

/**
 * @Author: driverSkr
 * @Time: 2023/11/24 16:54
 * @Description: 定位封装$
 */
class WeatherHubLocation private constructor(context: Context) {

    //定位监听
    private var weatherHubLocationListener: WeatherHubLocationListener? = null

    init {
        initLocation(context)
    }

    /**
     * 初始化定位
     */
    private fun initLocation(context: Context) {
        try {
            weatherHubLocationListener = WeatherHubLocationListener()
            mLocationClient = LocationClient(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mLocationClient?.let {
            //注册定位监听
            it.registerLocationListener(weatherHubLocationListener)
            val option = LocationClientOption()
            //如果开发者需要获得当前点的地址信息，此处必须为true
            option.setIsNeedAddress(true)
            //可选，设置是否需要最新版本的地址信息。默认不需要，即参数为false
            option.setNeedNewVersionRgc(true)
            //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
            it.locOption = option
        }
    }

    /**
     * 需要定位的页面调用此方法进行接口回调处理
     */
    fun setCallback(callback: LocationCallback) {
        WeatherHubLocation.callback = callback
    }

    /**
     * 开始定位
     */
    fun startLocation() {
        mLocationClient?.start()
    }

    /**
     * 请求定位
     */
    private fun requestLocation() {
        mLocationClient?.requestLocation()
    }


    /**
     * 停止定位
     */
    private fun stopLocation() {
        mLocationClient?.stop()
    }

    /**
     * 内部类实现百度定位结果接收
     */
    inner class WeatherHubLocationListener: BDAbstractLocationListener() {

        private val tag = "WeatherHubLocationListener"

        override fun onReceiveLocation(bdLocation: BDLocation?) {
            bdLocation?.let {
                if (it.direction.isNaN()) {
                    logE(tag, "onReceiveLocation: 未获取区/县数据，您可以重新断开连接网络再尝试定位。")
                    requestLocation()
                }
                stopLocation()
                callback?.onReceiveLocation(it)
            }
        }
    }

    companion object {
        @Volatile
        private var mInstance: WeatherHubLocation? = null

        @SuppressLint("StaticFieldLeak")
        private var mLocationClient: LocationClient? = null
        //定位回调接口
        private var callback: LocationCallback? = null

        fun getInstance(context: Context): WeatherHubLocation {
            return mInstance ?: synchronized(WeatherHubLocation::class.java) {
                mInstance ?: WeatherHubLocation(context).also { mInstance = it }
            }
        }
    }
}