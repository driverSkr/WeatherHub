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
    }
}