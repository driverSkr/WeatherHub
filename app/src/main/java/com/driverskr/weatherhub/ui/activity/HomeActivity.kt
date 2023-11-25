package com.driverskr.weatherhub.ui.activity

import android.content.Intent
import com.driverskr.weatherhub.databinding.ActivityHomeBinding
import com.driverskr.weatherhub.ui.activity.vm.HomeViewModel
import com.driverskr.weatherhub.ui.base.BaseVmActivity

class HomeActivity: BaseVmActivity<ActivityHomeBinding, HomeViewModel>() {

    override fun bindView() = ActivityHomeBinding.inflate(layoutInflater)

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
}