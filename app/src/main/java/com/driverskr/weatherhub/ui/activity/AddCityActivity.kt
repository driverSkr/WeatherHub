package com.driverskr.weatherhub.ui.activity

import android.content.Intent
import android.text.TextUtils
import com.driverskr.weatherhub.bean.CityBean
import com.driverskr.weatherhub.bean.Location
import com.driverskr.weatherhub.databinding.ActivityAddCityBinding
import com.driverskr.weatherhub.ui.activity.vm.SearchViewModel
import com.driverskr.weatherhub.ui.base.BaseVmActivity

class AddCityActivity: BaseVmActivity<ActivityAddCityBinding, SearchViewModel>() {

    override fun bindView() = ActivityAddCityBinding.inflate(layoutInflater)

    /**
     * 接收数据
     * @param intent
     */
    override fun prepareData(intent: Intent?) {

    }

    /**
     * 初始化布局组件
     */
    override fun initView() {

    }

    /**
     * 处理事件
     */
    override fun initEvent() {

    }

    /**
     * 初始化数据
     */
    override fun initData() {

    }

    companion object {
        /**
         * location转citybean
         */
        @JvmStatic
        fun location2CityBean(location: Location): CityBean {
            var parentCity = location.adm2
            val adminArea = location.adm1
            val city = location.country
            if (TextUtils.isEmpty(parentCity)) {
                parentCity = adminArea
            }
            if (TextUtils.isEmpty(adminArea)) {
                parentCity = city
            }
            val cityBean = CityBean()
            cityBean.cityName = parentCity + " - " + location.name
            cityBean.cityId = location.id
            cityBean.cnty = city
            cityBean.adminArea = adminArea
            return cityBean
        }
    }
}