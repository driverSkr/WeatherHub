package com.driverskr.weatherhub.logic

import android.util.Log
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
        Log.d(TAG,"WeatherRepository-searchCity : $response")
        response
    }

    suspend fun nowWeather(location: String) = withContext(Dispatchers.IO) {
        val response = weatherHubNetwork.fetchNowWeather(location)
        Log.d(TAG,"WeatherRepository-nowWeather : $response")
        response
    }

    companion object {
        private val TAG = NetworkRepository::class.simpleName
        @Volatile
        private var instance: NetworkRepository? = null

        @JvmStatic
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: NetworkRepository().also { instance = it }
        }
    }
}