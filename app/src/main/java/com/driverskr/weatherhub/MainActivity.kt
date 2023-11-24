package com.driverskr.weatherhub

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.baidu.location.BDLocation
import com.driverskr.weatherhub.databinding.ActivityMainBinding
import com.driverskr.weatherhub.location.LocationCallback
import com.driverskr.weatherhub.location.WeatherHubLocation
import com.driverskr.weatherhub.utils.PermissionUtils

class MainActivity : AppCompatActivity(), LocationCallback {

    private lateinit var binding: ActivityMainBinding
    //权限数组
    private val permissions = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    private lateinit var weatherHubLocation: WeatherHubLocation

    private lateinit var permissionUtils: PermissionUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        weatherHubLocation = WeatherHubLocation.getInstance(this)
        weatherHubLocation.setCallback(this)

        permissionUtils = PermissionUtils(this)

        requestPermissions()
    }

    /**
     * 接收定位
     * @param bdLocation 定位数据
     */
    override fun onReceiveLocation(bdLocation: BDLocation) {
        val address = bdLocation.addrStr
        binding.location.text = address
    }

    private fun requestPermissions() {
        permissionUtils.requestPermissions(permissions) { granted ->
            if (granted) {
                // 权限已经获取到，开始定位
                weatherHubLocation.startLocation()
            }
        }
    }
}