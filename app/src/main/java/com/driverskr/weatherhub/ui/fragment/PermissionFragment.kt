package com.driverskr.weatherhub.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.driverskr.lib.extension.logE
import com.driverskr.lib.extension.toast

/**
 * @Author: driverSkr
 * @Time: 2023/11/30 11:44
 * @Description: $
 */
class PermissionFragment: Fragment() {

    private var param1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        openGPS()
    }

    private fun openGPS() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        // 判断是否有合适的应用能够处理该 Intent，并且可以安全调用 startActivity()。
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) {
                logE("PermissionFragment","打开gps: " + it.resultCode)
            }.launch(intent)
        } else {
            toast("该设备不支持位置服务")
        }
        val beginTransaction = parentFragmentManager.beginTransaction()
        beginTransaction.remove(this)
        beginTransaction.commitAllowingStateLoss()
    }

    companion object {
        @JvmStatic
        fun newInstance() = PermissionFragment().apply {
            arguments = Bundle().apply {  }
        }
    }
}