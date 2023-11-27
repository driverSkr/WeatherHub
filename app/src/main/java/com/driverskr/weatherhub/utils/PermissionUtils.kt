package com.driverskr.weatherhub.utils

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * @Author: driverSkr
 * @Time: 2023/11/24 17:50
 * @Description: 权限请求工具类$
 */
class PermissionUtils(private val activity: AppCompatActivity) {

    private lateinit var permissionCallback: (Boolean) -> Unit

    fun requestPermissions(permissions: Array<String>, callback: (Boolean) -> Unit) {
        permissionCallback = callback

        if (checkPermissions(permissions)) {
            // 权限已经授予
            permissionCallback(true)
        } else {
            // 请求权限
            activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
                val allPermissionsGranted = result.all { it.value }
                permissionCallback(allPermissionsGranted)
            }.launch(permissions)
        }
    }

    private fun checkPermissions(permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
    }
}