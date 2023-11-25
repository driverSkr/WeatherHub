package com.driverskr.weatherhub.ui.activity.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.driverskr.weatherhub.bean.Location
import com.driverskr.weatherhub.bean.Now
import com.driverskr.weatherhub.logic.NetworkRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 18:34
 * @Description: $
 */
class TestViewModel: ViewModel() {
    val searchResult = MutableLiveData<List<Location>>()

    val nowWeather = MutableLiveData<Now>()

    /**
     * 搜索城市
     */
    fun searchCity(keywords: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val result = NetworkRepository.getInstance().searchCity(keywords,"exact")
            result.let {
                searchResult.postValue(it.location)
            }
        }
    }

    /**
     * 获取实时天气
     */
    fun nowWeather(locationId: String) {
        viewModelScope.launch {
            val result = NetworkRepository.getInstance().nowWeather(locationId)
            result.let {
                nowWeather.postValue(it.now)
            }
        }
    }
}