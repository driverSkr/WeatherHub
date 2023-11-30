package com.driverskr.weatherhub.logic

import com.driverskr.lib.extension.logD
import com.driverskr.weatherhub.bean.UserInfoBean
import com.driverskr.weatherhub.logic.network.WeatherHubNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 11:54
 * @Description: $
 */
class NetworkRepository {

    private val weatherHubNetwork: WeatherHubNetwork = WeatherHubNetwork.getInstance()

    suspend fun searchCity(location: String, mode: String) = withContext(Dispatchers.IO) {
        val response = weatherHubNetwork.fetchSearchCity(location, mode)
        logD(TAG,"WeatherRepository-searchCity : $response")
        response
    }

    suspend fun nowWeather(location: String) = withContext(Dispatchers.IO) {
        val response = weatherHubNetwork.fetchNowWeather(location)
        logD(TAG,"WeatherRepository-nowWeather : $response")
        response
    }

    suspend fun checkVersion() = withContext(Dispatchers.IO) {
        val response = weatherHubNetwork.fetchCheckVersion()
        logD(TAG,"WeatherRepository-checkVersion : $response")
        response
    }

    suspend fun register(userInfo: UserInfoBean) = withContext(Dispatchers.IO) {
        val response = weatherHubNetwork.fetchRegister(userInfo)
        logD(TAG,"WeatherRepository-register : $response")
        response
    }

    suspend fun warning(location: String) = withContext(Dispatchers.IO) {
        val response = weatherHubNetwork.fetchWarning(location)
        logD(TAG,"WeatherRepository-warning : $response")
        response
    }

    suspend fun dailyWeather(location: String) = withContext(Dispatchers.IO) {
        val response = weatherHubNetwork.fetchDailyWeather(location)
        logD(TAG,"WeatherRepository-dailyWeather : $response")
        response
    }

    suspend fun lifestyle(location: String) = withContext(Dispatchers.IO) {
        val response = weatherHubNetwork.fetchLifestyle(location)
        logD(TAG,"WeatherRepository-lifestyle : $response")
        response
    }

    suspend fun hourlyWeather(location: String) = withContext(Dispatchers.IO) {
        val response = weatherHubNetwork.fetchHourlyWeather(location)
        logD(TAG,"WeatherRepository-hourlyWeather : $response")
        response
    }

    suspend fun airWeather(location: String) = withContext(Dispatchers.IO) {
        val response = weatherHubNetwork.fetchAirWeather(location)
        logD(TAG,"WeatherRepository-airWeather : $response")
        response
    }

    suspend fun feedBack(content: String) = withContext(Dispatchers.IO) {
        val response = weatherHubNetwork.fetchFeedBack(content)
        logD(TAG,"WeatherRepository-feedBack : $response")
        response
    }

    companion object {
        private const val TAG = "NetworkRepository"
        @Volatile
        private var instance: NetworkRepository? = null

        @JvmStatic
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: NetworkRepository().also { instance = it }
        }
    }
}