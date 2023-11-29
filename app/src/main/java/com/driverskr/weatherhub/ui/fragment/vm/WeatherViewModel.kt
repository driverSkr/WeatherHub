package com.driverskr.weatherhub.ui.fragment.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.driverskr.weatherhub.bean.*
import com.driverskr.weatherhub.logic.DBRepository
import com.driverskr.weatherhub.logic.NetworkRepository
import com.driverskr.weatherhub.ui.base.BaseViewModel

/**
 * @Author: driverSkr
 * @Time: 2023/11/27 16:18
 * @Description: $
 */
const val CACHE_WEATHER_NOW = "now_"
const val CACHE_LIFE_INDICATOR = "now_life_indicator_"

class WeatherViewModel(val app: Application) : BaseViewModel(app) {

    val weatherNow = MutableLiveData<Now>()
    val warnings = MutableLiveData<List<Warning>>()
    val airNow = MutableLiveData<Air>()
    val forecast = MutableLiveData<List<Daily>>()
    val hourly = MutableLiveData<List<Hourly>>()
    val lifeIndicator = MutableLiveData<LifeIndicator>()

    fun loadCache(cityId: String) {
        launchSilent {
            val cache = DBRepository.getInstance().getCache<Now>(CACHE_WEATHER_NOW + cityId)
            cache?.let {
                weatherNow.postValue(it)
            }
        }
    }

    fun loadData(cityId: String) {
        // 实时天气
        launch {
            val result = NetworkRepository.getInstance().nowWeather(cityId)
            result.let {
                weatherNow.postValue(it.now)
                DBRepository.getInstance().saveCache(CACHE_WEATHER_NOW + cityId, it.now)
            }
        }
        // 预警
        launch {
            val result = NetworkRepository.getInstance().warning(cityId)
            result.let {
                if (it.warning.isNotEmpty()) {
                    warnings.postValue(result.warning)
                }
            }
        }
        // 实时空气
        launch {
            val result = NetworkRepository.getInstance().airWeather(cityId)
            result.let {
                airNow.postValue(it.now)
            }
        }
        // 7天 天气预报
        launch {
            val result = NetworkRepository.getInstance().dailyWeather(cityId)
            result.let {
                forecast.postValue(it.daily)
            }
        }
        // 逐小时天气预报
        launch {
            val result = NetworkRepository.getInstance().hourlyWeather(cityId)
            result.let {
                hourly.postValue(it.hourly)
            }
        }
        // 天气生活指数(使用缓存 3h)
        launch {
            val lifeIndicatorCacheKey = CACHE_LIFE_INDICATOR + cityId
            val cache = DBRepository.getInstance().getCache<LifeIndicator>(lifeIndicatorCacheKey)
            cache?.let {
                lifeIndicator.postValue(it)
                return@launch
            }
            NetworkRepository.getInstance().lifestyle(cityId).let {
                DBRepository.getInstance().saveCache(lifeIndicatorCacheKey, it, 3 * 60 * 60)
                lifeIndicator.postValue(it)
            }
        }
    }
}