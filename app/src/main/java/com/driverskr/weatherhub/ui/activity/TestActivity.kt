package com.driverskr.weatherhub.ui.activity

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.baidu.location.BDLocation
import com.driverskr.lib.extension.logD
import com.driverskr.weatherhub.databinding.ActivityTestBinding
import com.driverskr.weatherhub.location.LocationCallback
import com.driverskr.weatherhub.location.WeatherHubLocation
import com.driverskr.weatherhub.ui.activity.vm.TestViewModel
import com.driverskr.weatherhub.utils.PermissionUtils

class TestActivity : AppCompatActivity(), LocationCallback {
    private lateinit var binding: ActivityTestBinding

    //权限数组
    private val permissions = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private lateinit var weatherHubLocation: WeatherHubLocation

    private lateinit var viewModel: TestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(TestViewModel::class.java)
        viewModel.searchResult.observe(this) {
            var i = 1
            for (location in it) {
                logD(TAG,"所有城市结果$i：$location")
                i++
            }
            viewModel.nowWeather(it[0].id)
        }
        viewModel.nowWeather.observe(this) {
            logD(TAG,"实时天气：$it")
        }

        weatherHubLocation = WeatherHubLocation.getInstance(this)
        weatherHubLocation.setCallback(this)

        PermissionUtils(this).requestPermissions(permissions) { granted ->
            if (granted) {
                // 权限已经获取到，开始定位
                weatherHubLocation.startLocation()
            }
        }
    }

    /**
     * 接收定位
     * @param bdLocation 定位数据
     */
    override fun onReceiveLocation(bdLocation: BDLocation) {
        val address = bdLocation.addrStr
        binding.tvLocation.text = address

        viewModel.searchCity(address)
    }

    companion object {
        private const val TAG = "TestActivity"
    }
}