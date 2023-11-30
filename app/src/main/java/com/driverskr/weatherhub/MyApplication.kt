package com.driverskr.weatherhub

import com.baidu.location.LocationClient
import com.driverskr.lib.BaseApplication
import com.driverskr.lib.utils.SpUtil
import com.driverskr.plugin_lib.SkinManager

/**
 * @Author: driverSkr
 * @Time: 2023/11/24 17:47
 * @Description: $
 */
class MyApplication: BaseApplication() {

    override fun onCreate() {
        super.onCreate()
        //使用定位需要同意隐私合规政策
        LocationClient.setAgreePrivacy(true)

        SkinManager.init(this, object : SkinManager.OnPluginCallback {
            override fun setSkin(skinPath: String?) {
                SpUtil.getInstance(context).pluginPath = skinPath
            }

            override fun reset() {
                SpUtil.getInstance(context).themeFlag = 0
            }

            override fun getSkin(): String {
                return SpUtil.getInstance(context).pluginPath
            }
        })
    }
}