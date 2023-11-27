package com.driverskr.weatherhub.logic.network

import com.driverskr.lib.extension.logD
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 10:53
 * @Description: service构建器$
 */
object ServiceCreator {

    private fun <T> create(serviceClass: Class<T>, apiType: ApiType): T {
        val baseUrl = getBaseUrl(apiType)
        logD("driverSkr", "ServiceCreator : $baseUrl")
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(serviceClass)
    }


    fun <T : Any> createService(serviceClass: Class<T>, apiType: ApiType) = create(serviceClass, apiType)

    private fun getBaseUrl(apiType: ApiType): String {
        return when (apiType) {
            ApiType.SEARCH -> "https://geoapi.qweather.com"  //和风天气搜索城市
            ApiType.WEATHER -> "https://devapi.qweather.com" //和风天气API
            ApiType.BING -> "https://cn.bing.com"    //必应壁纸
            ApiType.FENGYUN -> "https://fengyun.icu"    //版本
        }
    }
}