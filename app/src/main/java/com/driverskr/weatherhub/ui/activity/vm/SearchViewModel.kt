package com.driverskr.weatherhub.ui.activity.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.driverskr.weatherhub.R
import com.driverskr.weatherhub.bean.CityBean
import com.driverskr.weatherhub.bean.Location
import com.driverskr.weatherhub.logic.DBRepository
import com.driverskr.weatherhub.logic.NetworkRepository
import com.driverskr.weatherhub.logic.db.entity.CityEntity
import com.driverskr.weatherhub.ui.base.BaseViewModel
import com.driverskr.weatherhub.utils.Constant
import java.util.ArrayList

/**
 * @Author: driverSkr
 * @Time: 2023/11/27 11:18
 * @Description: 搜索数据$
 */
const val LAST_LOCATION = "LAST_LOCATION"

class SearchViewModel(private val app: Application): BaseViewModel(app) {

    val searchResult = MutableLiveData<List<Location>?>()

    val curCity = MutableLiveData<Location>()

    val choosedCity = MutableLiveData<Location>()

    val topCity = MutableLiveData<List<String>>()

    val addFinish = MutableLiveData<Boolean>()

    val curLocation = MutableLiveData<String>()

    val cacheLocation = MutableLiveData<String>()

    /**
     * 搜索城市
     */
    fun searchCity(keywords: String) {
        launchSilent {
            val result = NetworkRepository.getInstance().searchCity(keywords,"exact")
            result?.let {
                searchResult.postValue(it.location)
            }
        }
    }

    /**
     * 获取热门城市
     */
    fun getTopCity() {
        launch {
            val stringArray = app.resources.getStringArray(R.array.top_city)
            val cityList = stringArray.toList() as ArrayList<String>
            topCity.postValue(cityList)
        }
    }

    /**
     * 添加城市
     */
    fun addCity(it: CityBean, isLocal: Boolean = false, fromSplash: Boolean = false) {
        launch {
            if (isLocal) {
                DBRepository.getInstance().removeLocal(it.cityId)
            }
            DBRepository.getInstance().addCity(CityEntity(it.cityId, it.cityName, isLocal))
            Constant.CITY_CHANGE = true
            if (!isLocal) {
                addFinish.postValue(true)
            } else if (fromSplash) {
                addFinish.postValue(true)
            }
        }
    }

    /**
     * 获取城市数据
     */
    fun getCityInfo(cityName: String, save: Boolean = false) {
        launch {
            if (save) {
                // 缓存定位城市
                DBRepository.getInstance().saveCache(LAST_LOCATION, cityName)
            }

            val result = NetworkRepository.getInstance().searchCity(cityName,"exact")
            result?.let {
                if (save) {
                    curCity.postValue(it.location[0])
                } else {
                    choosedCity.postValue(it.location[0])
                }
            }
        }
    }

    fun getCacheLocation() {
        launch {
            (DBRepository.getInstance().getCache<String>(LAST_LOCATION)).let {
                cacheLocation.postValue(it)
            }
        }
    }
}