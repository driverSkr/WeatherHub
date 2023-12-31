package com.driverskr.weatherhub.ui.activity

import android.content.Intent
import com.driverskr.weatherhub.R
import com.driverskr.weatherhub.databinding.ActivitySettingsBinding
import com.driverskr.weatherhub.ui.base.BaseActivity
import com.driverskr.weatherhub.ui.fragment.SettingsFragment

class SettingsActivity: BaseActivity<ActivitySettingsBinding>() {

    override fun bindView() = ActivitySettingsBinding.inflate(layoutInflater)

    /**
     * 接收数据
     * @param intent
     */
    override fun prepareData(intent: Intent?) {}

    /**
     * 初始化布局组件
     */
    override fun initView() {
        setTitle(getString(R.string.setting))

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()
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