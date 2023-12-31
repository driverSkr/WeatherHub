package com.driverskr.weatherhub.service

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.*
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.driverskr.lib.extension.logD
import com.driverskr.lib.extension.logE
import com.driverskr.lib.utils.IconUtils
import com.driverskr.weatherhub.R
import com.driverskr.weatherhub.bean.Now
import com.driverskr.weatherhub.logic.DBRepository
import com.driverskr.weatherhub.logic.NetworkRepository
import com.driverskr.weatherhub.ui.activity.SplashActivity
import com.driverskr.weatherhub.ui.fragment.vm.CACHE_WEATHER_NOW
import com.driverskr.weatherhub.utils.Lunar
import com.driverskr.weatherhub.utils.NotificationUtil
import com.driverskr.weatherhub.utils.RomUtil
import com.driverskr.weatherhub.widget.WeatherWidget
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.*

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                Notify_Id,
                NotificationUtil.createNotification(this, Notify_Id),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(
                Notify_Id,
                NotificationUtil.createNotification(this, Notify_Id)
            )
        }

        connManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        //如果 Android 版本是 Nougat（7.0）或更高
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //注册网络回调
            connManager.registerDefaultNetworkCallback(callback)
        } else {
            val intentFilter = IntentFilter()
            intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE")
            //注册一个用于监听连接状态变化的广播接收器。
            registerReceiver(netWorkStateReceiver, intentFilter)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (isFirst) {
            //第一次运行onStartCommand时不需要更新数据
            isFirst = false
        } else {
            //更新数据
            lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, _ -> }) {
                updateRemoteOnce()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * 网络连接回调
     * * 定义了一个用于处理网络连接变化的回调对象。
     */
    private val callback = @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    object : ConnectivityManager.NetworkCallback() {
        //当网络可用时
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            logD(TAG,"network available。。。。")
            //更新数据
            updateRemote()
        }
        //当网络不可用时
        override fun onLost(network: Network) {
            super.onLost(network)
            logD(TAG,"network unavailable。。。。")
            //取消定时任务 (intervalJob)
            intervalJob?.cancel()
            intervalJob = null
        }
    }

    //定义了一个用于监听网络状态变化的广播接收器
    private val netWorkStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val activeNetworkInfo = connManager.activeNetworkInfo
            //当网络可用时
            if (activeNetworkInfo != null && activeNetworkInfo.isAvailable) {
                logD(TAG,"network available。。。。")
                //更新数据
                updateRemote()
            }
            //当网络不可用时
            else {
                logD(TAG,"network unavailable。。。。")
                //取消定时任务 (intervalJob)
                intervalJob?.cancel()
                intervalJob = null
            }
        }
    }

    private suspend fun getWeather(cityId: String) {
        flow {
            NetworkRepository.getInstance().nowWeather(cityId).now.let { emit(it) }
        }.flowOn(Dispatchers.Main).collect {

        }
    }

    private var intervalJob: Job? = null

    /**
     * 用于启动一个定时任务，定时执行 updateRemoteOnce()
     */
    private fun updateRemote() {
        if (intervalJob != null) {
            return
        }
        intervalJob = lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, _ ->
            logE("WidgetService","WidgetService: 异常...")
        }) {
            while (isActive) {
                logD(TAG,"intervalJob run")
                updateRemoteOnce()
                delay(1800_000)
            }
        }
    }

    /**
     * 获取城市信息，然后使用 NetworkRepository 获取当前天气信息，并更新通知和小部件
     */
    private suspend fun updateRemoteOnce() {
        val cities = DBRepository.getInstance().getCities()
        if (cities.isNotEmpty()) {
            var cityId = cities[0].cityId
            var cityName = cities[0].cityName
            cities.forEach {
                if (it.isLocal) {
                    cityId = it.cityId
                    cityName = it.cityName
                }
                return@forEach
            }

            val result = NetworkRepository.getInstance().nowWeather(cityId)
            result.let {
                val now = it.now

                NotificationUtil.updateNotification(
                    this@WidgetService,
                    Notify_Id,
                    cityName,
                    now
                )

                updateWidget(cityId, cityName, now)
            }
        }
    }

    //小部件更新
    @SuppressLint("RemoteViewLayout")
    private suspend fun updateWidget(cityId: String, cityName: String, now: Now?) {
        logD("WidgetService","updateWidget.............")

        val views = RemoteViews(packageName, R.layout.weather_widget)
        val location = if (cityName.contains("-")) cityName.split("-")[1] else cityName
        views.setTextViewText(R.id.tvLocation, location)

        now?.let {
            DBRepository.getInstance()
                .saveCache(CACHE_WEATHER_NOW + cityId, it)

            views.setTextViewText(R.id.tvWeather, it.text)
            views.setTextViewText(R.id.tvTemp, it.temp + "°C")
            if (IconUtils.isDay()) {
                views.setImageViewResource(R.id.ivWeather, IconUtils.getDayIconDark(this, it.icon))
            } else {
                views.setImageViewResource(
                    R.id.ivWeather,
                    IconUtils.getNightIconDark(this, it.icon)
                )
            }
        }
        views.setTextViewText(R.id.tvLunarDate, Lunar(Calendar.getInstance()).toString())

        initEvent(views)

        val componentName = ComponentName(this, WeatherWidget::class.java)
        AppWidgetManager.getInstance(this).updateAppWidget(componentName, views)
    }

    /**
     * 点击事件相关
     */
    private fun initEvent(views: RemoteViews) {
        // 日历
        val calendarIntent = Intent()

        val calendarCls = getCalendarCls()
        calendarIntent.component = ComponentName(calendarCls.first, calendarCls.second)
        val calendarPI = PendingIntent.getActivity(
            this, 0, calendarIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.llCalendar, calendarPI)
        views.setOnClickPendingIntent(R.id.tvLunarDate, calendarPI)

        // 时钟
        val clockIntent = Intent()

        val clockComponent = getClockComponent()
        clockIntent.component = ComponentName(clockComponent.first, clockComponent.second)
        val timePI = PendingIntent.getActivity(this, 0, clockIntent, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.clockTime, timePI)

        // 风云
        val weatherIntent = Intent(this, SplashActivity::class.java)
        val weatherPI =
//            PendingIntent.getActivity(this, 0, weatherIntent, PendingIntent.FLAG_MUTABLE)
            PendingIntent.getActivity(this, 0, weatherIntent, PendingIntent.FLAG_IMMUTABLE)
        views.setOnClickPendingIntent(R.id.llWeather, weatherPI)
    }

    private fun getCalendarCls(): Pair<String, String> {
        return when {
            RomUtil.isMiui() -> "com.android.calendar" to "com.android.calendar.homepage.AllInOneActivity"
            RomUtil.isOppo() -> "com.coloros.calendar" to "com.android.calendar.AllInOneActivity"
            else -> "com.android.calendar" to "com.android.calendar.LaunchActivity"
        }
    }

    private fun getClockComponent(): Pair<String, String> {
        return when {
            RomUtil.isEmui() -> "com.android.deskclock" to "com.android.deskclock.AlarmsMainActivity"
            RomUtil.isMiui() -> "com.android.deskclock" to "com.android.deskclock.DeskClockTabActivity"
            RomUtil.isOppo() -> "com.coloros.alarmclock" to "com.coloros.alarmclock.AlarmClock"
            else -> "com.android.deskclock" to "com.android.deskclock.DeskClock"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connManager.unregisterNetworkCallback(callback)
        } else {
            unregisterReceiver(netWorkStateReceiver)
        }
        logE(TAG,"onDestroy: ---------------------")
    }

    companion object {
        private const val TAG = "WidgetService"
    }
}