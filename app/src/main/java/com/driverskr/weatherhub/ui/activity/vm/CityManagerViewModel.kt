package com.driverskr.weatherhub.ui.activity.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.driverskr.weatherhub.logic.DBRepository
import com.driverskr.weatherhub.logic.db.entity.CityEntity
import com.driverskr.weatherhub.ui.base.BaseViewModel

/**
 * @Author: driverSkr
 * @Time: 2023/11/30 14:36
 * @Description: 城市管理$
 */
class CityManagerViewModel(app: Application): BaseViewModel(app) {

    val cities = MutableLiveData<List<CityEntity>>()

    fun getCities() {
        launch {
            val results = DBRepository.getInstance().getCities()
            cities.postValue(results)
        }
    }

    fun removeCity(cityId: String) {
        launch {
            DBRepository.getInstance().removeCity(cityId)
        }
    }

    fun updateCities(it: List<CityEntity>) {
        launch {
            DBRepository.getInstance().removeNotLocalCity()
            it.forEach {
                DBRepository.getInstance().addCity(it)
            }
        }
    }
}