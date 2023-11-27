package com.driverskr.weatherhub.ui.fragment

import android.os.Bundle
import android.view.View
import com.driverskr.weatherhub.databinding.FragmentWeatherBinding
import com.driverskr.weatherhub.ui.base.BaseVmFragment
import com.driverskr.weatherhub.ui.fragment.vm.WeatherViewModel

/**
 * @Author: driverSkr
 * @Time: 2023/11/27 15:24
 * @Description: $
 */
class WeatherFragment: BaseVmFragment<FragmentWeatherBinding, WeatherViewModel>() {

    private val PARAM_CITY_ID = "param_city_id"

    override fun bindView() = FragmentWeatherBinding.inflate(layoutInflater)

    override fun initView(view: View?) {

    }

    override fun initEvent() {

    }

    /**
     * 数据初始化，只会执行一次
     */
    override fun loadData() {

    }

    fun changeUnit() {
        /*nowTmp?.let {
            showTempByUnit()
        }*/
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String?) =
            WeatherFragment().apply {
                arguments = Bundle().apply {
                    putString(PARAM_CITY_ID, param1)
                }
            }
    }
}