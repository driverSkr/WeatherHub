package com.driverskr.weatherhub.service

import android.content.Intent
import android.net.ConnectivityManager
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.driverskr.lib.extension.logE

/**
 * @Author: driverSkr
 * @Time: 2023/11/30 13:44
 * @Description: $
 */

const val Notify_Id = 999

class WidgetService : LifecycleService() {

    lateinit var connManager: ConnectivityManager

    /**
     * 防止Service首次启动时执行onStartCommand()中的updateRemoteOnce()
     */
    private var isFirst = true

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onCreate() {
        super.onCreate()
        isFirst = true
        logE(TAG,"onCreate: ---------------------")
    }

    companion object {
        private const val TAG = "WidgetService"
    }
}