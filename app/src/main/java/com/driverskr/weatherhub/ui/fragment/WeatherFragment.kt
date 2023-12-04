package com.driverskr.weatherhub.ui.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.TextViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.driverskr.lib.extension.logD
import com.driverskr.lib.extension.logE
import com.driverskr.lib.extension.notEmpty
import com.driverskr.lib.utils.DateUtil
import com.driverskr.lib.utils.Typefaces
import com.driverskr.lib.utils.WeatherUtil
import com.driverskr.weatherhub.R
import com.driverskr.weatherhub.adapter.Forecast3dAdapter
import com.driverskr.weatherhub.adapter.Forecast7dAdapter
import com.driverskr.weatherhub.bean.*
import com.driverskr.weatherhub.databinding.*
import com.driverskr.weatherhub.dialog.AlarmDialog
import com.driverskr.weatherhub.dialog.LifeIndexDialog
import com.driverskr.weatherhub.ui.activity.vm.HomeViewModel
import com.driverskr.weatherhub.ui.base.BaseVmFragment
import com.driverskr.weatherhub.ui.fragment.vm.WeatherViewModel
import com.driverskr.weatherhub.utils.Constant
import com.driverskr.weatherhub.utils.DisplayUtil
import com.driverskr.weatherhub.utils.Lunar
import java.util.*

/**
 * @Author: driverSkr
 * @Time: 2023/11/27 15:24
 * @Description: $
 */
class WeatherFragment: BaseVmFragment<FragmentWeatherBinding, WeatherViewModel>() {

    private val PARAM_CITY_ID = "param_city_id"

    private lateinit var mCityId: String

    private var todayWeather: Daily? = null

    private var hasAni = false

    private var nowTmp: String? = null

    private var condCode: String? = null

    private var mForecastAdapter3d: Forecast3dAdapter? = null

    private var mForecastAdapter7d: Forecast7dAdapter? = null

    private val mForecastList by lazy { ArrayList<Daily>() }

    private lateinit var todayBriefInfoBinding: LayoutTodayBriefInfoBinding

    private lateinit var forecastHourlyBinding: LayoutForecastHourlyBinding

    private lateinit var forecast7dBinding: LayoutForecast7dBinding

    private lateinit var airQualityBinding: LayoutAirQualityBinding

    private lateinit var lifeIndicatorBinding: LayoutLifeIndicatorBinding

    private lateinit var sunMoonBinding: LayoutSunMoonBinding

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mCityId = it.getString(PARAM_CITY_ID).toString()
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.loadCache(mCityId)

        Calendar.getInstance().apply {
            val day = get(Calendar.DAY_OF_MONTH)
            ((get(Calendar.MONTH) + 1).toString() + "月" + day + "日 农历" +
                    Lunar(this).toString()).also { mBinding.tvDate.text = it }
        }
    }

    override fun onResume() {
        super.onResume()
        condCode?.let {
            logD("$TAG onResume()","onResume() set cond code: $condCode")
            homeViewModel.setCondCode(it)
        }

        changeUnit()
        setViewTime()
    }

    override fun bindView() = FragmentWeatherBinding.inflate(layoutInflater)

    override fun initView(view: View?) {
        // must use activity
        homeViewModel = ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        todayBriefInfoBinding = LayoutTodayBriefInfoBinding.bind(mBinding.root)

        forecastHourlyBinding = LayoutForecastHourlyBinding.bind(mBinding.root)

        forecast7dBinding = LayoutForecast7dBinding.bind(mBinding.root)

        sunMoonBinding = LayoutSunMoonBinding.bind(mBinding.root)

        airQualityBinding = LayoutAirQualityBinding.bind(mBinding.root)

        lifeIndicatorBinding = LayoutLifeIndicatorBinding.bind(mBinding.root)

        // 设置字体
        mBinding.tvTodayTmp.typeface = Typefaces.get(requireContext(), "Futura-CondensedMedium.ttf")

        for (i in 0 until 3) {
            mForecastList.add(Daily(iconDay = "100", textDay = "晴", tempMin = "20", tempMax = "25"))
        }
        mForecastAdapter3d = Forecast3dAdapter(requireContext(), mForecastList)
        mBinding.rvForecast3.adapter = mForecastAdapter3d
        val forecastManager = GridLayoutManager(requireContext(), 3)
        mBinding.rvForecast3.layoutManager = forecastManager

        mForecastAdapter7d = Forecast7dAdapter(requireContext(), mForecastList)
        forecast7dBinding.rvForecast7.adapter = mForecastAdapter7d
        forecast7dBinding.rvForecast7.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
    }

    override fun initEvent() {
        mBinding.swipeLayout.setOnRefreshListener { loadData() }

        viewModel.weatherNow.observe(this) {
            showWeatherNow(it)
        }

        viewModel.warnings.observe(this) {
            showWarnings(it)
        }

        viewModel.airNow.observe(this) {
            showAirNow(it)
        }

        viewModel.forecast.observe(this) {
            showForecast(it)
        }
        viewModel.hourly.observe(this) {
            showHourly(it)
        }

        viewModel.lifeIndicator.observe(this) {
            showIndicator(it)
        }

        viewModel.loadState.observe(this) {
            when (it) {
                is LoadState.Start -> {
                    mBinding.swipeLayout.isRefreshing = true
                }
                is LoadState.Error -> {
                }
                is LoadState.Finish -> {
                    if (viewModel.isStopped()) {
                        mBinding.swipeLayout.isRefreshing = false
                    }
                }
            }
        }
    }

    /**
     * 数据初始化，只会执行一次
     */
    override fun loadData() {
        viewModel.loadData(mCityId)
    }

    @SuppressLint("SetTextI18n")
    fun showWeatherNow(now: Now) {
        condCode = now.icon
        nowTmp = now.temp
        lifecycleScope.launchWhenResumed {
            homeViewModel.setCondCode(now.icon)
        }
        mBinding.tvTodayCond.text = now.text
        mBinding.tvUnit.visibility = View.VISIBLE

        showTempByUnit()

        todayBriefInfoBinding.tvHumidity.text = now.humidity + "%"
        todayBriefInfoBinding.tvWindScale.text = now.windDir + now.windScale + "级"
        todayBriefInfoBinding.tvPressure.text = now.pressure + "hpa"
    }

    @SuppressLint("SetTextI18n")
    private fun showTempByUnit() {
        if (Constant.APP_SETTING_UNIT == TempUnit.HUA.tag) {
            val tempHua = WeatherUtil.getF(nowTmp!!)
            mBinding.tvTodayTmp.text = tempHua.toString()
            mBinding.tvUnit.text = "°F"
            todayBriefInfoBinding.tvFeelTemp.text = "$tempHua°F"
        } else {
            todayBriefInfoBinding.tvFeelTemp.text = "$nowTmp°C"
            mBinding.tvTodayTmp.text = nowTmp
            mBinding.tvUnit.text = "°C"
        }
    }

    /**
     * 三天预报
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun showForecast(dailyForecast: List<Daily>) {
        val currentTime = DateUtil.getNowTime()
        val forecastBase = dailyForecast[0]
        todayWeather = forecastBase

        sunMoonBinding.sunView.setTimes(todayWeather?.sunrise, todayWeather?.sunset, currentTime)
        sunMoonBinding.moonView.setTimes(todayWeather?.moonrise, todayWeather?.moonset, currentTime)

        sunMoonBinding.tvMoonPhrase.text = todayWeather?.moonPhase

        mForecastList.clear()
        mForecastList.addAll(dailyForecast)

        mForecastAdapter3d?.notifyDataSetChanged()
        var min = forecastBase.tempMin.toInt()
        var max = forecastBase.tempMax.toInt()
        mForecastList.forEach {
            min = it.tempMin.toInt().coerceAtMost(min)
            max = it.tempMax.toInt().coerceAtLeast(max)
        }

        mForecastAdapter7d?.setRange(min, max)
    }

    /**
     * 空气质量
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun showAirNow(airNow: Air) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBinding.tvAirCondition.text =
                getString(R.string.air_condition, airNow.aqi, airNow.category)

            TextViewCompat.setCompoundDrawableTintList(
                mBinding.tvAirCondition, ColorStateList.valueOf(
                    WeatherUtil.getAirColor(
                        requireContext(),
                        airNow.aqi
                    )
                )
            )
            mBinding.tvAirCondition.visibility = View.VISIBLE
        } else {
            mBinding.tvAirCondition.visibility = View.GONE
        }
        airQualityBinding.airConditionView.setValue(airNow.aqi.toInt(), airNow.category)

        airQualityBinding.tvTodayPm25.text = airNow.pm2p5
        airQualityBinding.tvTodaySo2.text = airNow.so2
        airQualityBinding.tvTodayCo.text = airNow.co
        airQualityBinding.tvTodayPm10.text = airNow.pm10
        airQualityBinding.tvTodayNo2.text = airNow.no2
        airQualityBinding.tvTodayO3.text = airNow.o3
    }

    /**
     * 预警
     */
    private fun showWarnings(warnings: List<Warning>) {
        mBinding.alarmFlipper.visibility = View.VISIBLE
        mBinding.alarmFlipper.setInAnimation(requireContext(), R.anim.bottom_in)
        mBinding.alarmFlipper.setOutAnimation(requireContext(), R.anim.top_out)
        mBinding.alarmFlipper.flipInterval = 4000
        for (warning in warnings) {
            val level: String = warning.level
            val tip = warning.typeName + level + "预警"
            val warningRes = WeatherUtil.getWarningRes(requireContext(), level)
            val textView: TextView = layoutInflater.inflate(R.layout.item_warning, null) as TextView
            textView.background = warningRes.first
            textView.text = tip
            textView.setOnClickListener {
                AlarmDialog(requireContext()).apply {
                    setContent(tip, warning.text)
                    show()
                }
            }
            textView.setTextColor(warningRes.second)
            mBinding.alarmFlipper.addView(textView)
        }
        if (warnings.size > 1) {
            mBinding.alarmFlipper.startFlipping()
        }
    }

    /**
     * 逐小时天气
     */
    private fun showHourly(hourlyWeatherList: List<Hourly>) {
        val data: MutableList<Hourly> = ArrayList()
        val end = if (hourlyWeatherList.size > 23) 23 else hourlyWeatherList.size - 1
        for (i in 0..end) {
            data.add(hourlyWeatherList[i])
            val condCode = data[i].icon
            var time = data[i].fxTime
            time = time.substring(time.length - 11, time.length - 9)
            val hourNow = time.toInt()
            if (hourNow in 6..19) {
                data[i].icon = condCode + "d"
            } else {
                data[i].icon = condCode + "n"
            }
        }
        var minTmp = data[0].temp.toInt()
        var maxTmp = minTmp
        for (i in data.indices) {
            val tmp = data[i].temp.toInt()
            minTmp = tmp.coerceAtMost(minTmp)
            maxTmp = tmp.coerceAtLeast(maxTmp)
        }
        // 设置当天的最高最低温度
        forecastHourlyBinding.hourly.setHighestTemp(maxTmp)
        forecastHourlyBinding.hourly.setLowestTemp(minTmp)
        if (maxTmp == minTmp) {
            forecastHourlyBinding.hourly.setLowestTemp(minTmp - 1)
        }
        forecastHourlyBinding.hourly.initData(data)

        val tempRange = if (Constant.APP_SETTING_UNIT == TempUnit.HUA.tag) {
            WeatherUtil.getF(minTmp.toString()).toString() + " ~ " +
                    WeatherUtil.getF(maxTmp.toString()) + "°F"
        } else {
            "$minTmp ~ $maxTmp°C"
        }
        forecastHourlyBinding.tvLineTmpRange.text = tempRange

    }

    /**
     * 显示生活指数
     */
    private fun showIndicator(lifeIndicator: LifeIndicator) {
        val daily = lifeIndicator.daily
        lifeIndicatorBinding.tvIndicatorSport.text = daily[0].category
        lifeIndicatorBinding.tvIndicatorWear.text = daily[2].category
        lifeIndicatorBinding.tvIndicatorUv.text = daily[3].category
        lifeIndicatorBinding.tvIndicatorCold.text = daily[4].category
        lifeIndicatorBinding.tvIndicatorCar.text = daily[1].category
        lifeIndicatorBinding.tvIndicatorDrying.text = daily[5].category

        //点击弹窗
        lifeIndicatorBinding.llSport.setOnClickListener { initLifeIndexDialog(daily[0]) }
        lifeIndicatorBinding.llWear.setOnClickListener { initLifeIndexDialog(daily[2]) }
        lifeIndicatorBinding.llUv.setOnClickListener { initLifeIndexDialog(daily[3]) }
        lifeIndicatorBinding.llCold.setOnClickListener { initLifeIndexDialog(daily[4]) }
        lifeIndicatorBinding.llCar.setOnClickListener { initLifeIndexDialog(daily[1]) }
        lifeIndicatorBinding.llDrying.setOnClickListener { initLifeIndexDialog(daily[5]) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mBinding.root.parent != null) {
            (mBinding.root.parent as ViewGroup).removeView(mBinding.root)
        }
    }

    /**
     * 设置view的时间
     */
    private fun setViewTime() {
        if (!hasAni && todayWeather?.sunrise.notEmpty() && todayWeather?.sunset.notEmpty() && todayWeather?.moonrise.notEmpty() && todayWeather?.moonset.notEmpty()) {
            val currentTime = DateUtil.getNowTime()
            sunMoonBinding.sunView.setTimes(
                todayWeather?.sunrise,
                todayWeather?.sunset,
                currentTime
            )
            sunMoonBinding.moonView.setTimes(
                todayWeather?.moonrise,
                todayWeather?.moonset,
                currentTime
            )

            hasAni = true
        }
    }

    fun changeUnit() {
        nowTmp?.let {
            showTempByUnit()
        }
        loadData()
    }

    /**
     * 对"生活指数“弹窗的数据填充
     * @param dailyBean 数据源
     */
    private fun initLifeIndexDialog(dailyBean: LifeIndicatorDaily) {
        val dialog = LifeIndexDialog(context!!)
        val dialogBinding = DialogLifeIndexDetailBinding.inflate(LayoutInflater.from(context))
        dialogBinding.apply {
            tvTitle.text = dailyBean.name
            tvKind.text = dailyBean.category
            tvText.text = dailyBean.text
        }
        when (dailyBean.type) {
            "1" -> dialogBinding.ivContent.setImageResource(R.drawable.ic_indicator_sport)
            "2" -> dialogBinding.ivContent.setImageResource(R.drawable.ic_indicator_car)
            "3" -> dialogBinding.ivContent.setImageResource(R.drawable.ic_indicator_wear)
            "5" -> dialogBinding.ivContent.setImageResource(R.drawable.ic_indicator_uv)
            "9" -> dialogBinding.ivContent.setImageResource(R.drawable.ic_indicator_cold)
            "14" -> dialogBinding.ivContent.setImageResource(R.drawable.ic_indicator_drying)
            else -> logE(TAG,"生活指数弹窗创建出现异常")
        }
        dialog.showCenterPopupWindow(dialogBinding.root, DisplayUtil.dp2px(250f), DisplayUtil.dp2px(220f), true)
    }

    companion object {

        private const val TAG = "WeatherFragment"

        @JvmStatic
        fun newInstance(param1: String?) =
            WeatherFragment().apply {
                arguments = Bundle().apply {
                    putString(PARAM_CITY_ID, param1)
                }
            }
    }
}