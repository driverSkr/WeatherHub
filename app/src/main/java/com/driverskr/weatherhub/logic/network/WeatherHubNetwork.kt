package com.driverskr.weatherhub.logic.network

import com.driverskr.weatherhub.logic.network.api.LocationService
import com.driverskr.weatherhub.logic.network.api.WeatherService

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 11:41
 * @Description: 管理网络请求$
 */
class WeatherHubNetwork {

    private val weatherService = ServiceCreator.createService(WeatherService::class.java,ApiType.WEATHER)
    private val locationService = ServiceCreator.createService(LocationService::class.java,ApiType.SEARCH)

    suspend fun fetchSearchCity(location: String,mode: String) = locationService.searchCity(location, mode)

    suspend fun fetchNowWeather(location: String) = weatherService.nowWeather(location)

    companion object {

        @Volatile   //被 @Volatile 修饰的属性可能会被多个线程同时访问，因此编译器不应该进行某些优化，以确保对这个属性的读取和写入是原子的。
        private var INSTANCE: WeatherHubNetwork? = null

        fun getInstance(): WeatherHubNetwork = INSTANCE ?: synchronized(this) {
            INSTANCE ?: WeatherHubNetwork()
        }
    }
}