package com.driverskr.weatherhub.ui.activity.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.driverskr.weatherhub.bean.TempUnit
import com.driverskr.weatherhub.bean.VersionBean
import com.driverskr.weatherhub.logic.DBRepository
import com.driverskr.weatherhub.logic.NetworkRepository
import com.driverskr.weatherhub.logic.db.entity.CityEntity
import com.driverskr.weatherhub.ui.base.BaseViewModel
import com.driverskr.weatherhub.utils.Constant

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 13:53
 * @Description: $
 */
class HomeViewModel(val app: Application): BaseViewModel(app) {

    val mCities = MutableLiveData<List<CityEntity>>()

    val mCurCondCode = MutableLiveData<String>()

    val newVersion = MutableLiveData<VersionBean>()

    fun setCondCode(condCode: String) {
        mCurCondCode.postValue(condCode)
    }

    fun getCities() {
        launchSilent {
            val cities = DBRepository.getInstance().getCities()
            mCities.postValue(cities)
        }
    }

    fun checkVersion() {
        launchSilent {
            val result = NetworkRepository.getInstance().checkVersion()
            result.let {
                newVersion.postValue(it)
            }
        }
    }

    fun changeUnit(unit: TempUnit) {
        Constant.APP_SETTING_UNIT = unit.tag

        PreferenceManager.getDefaultSharedPreferences(app).edit().apply {
            putString("unit", unit.tag)
            apply()
        }
    }
}