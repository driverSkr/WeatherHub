package com.driverskr.weatherhub.ui.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.driverskr.lib.extension.logE
import com.driverskr.weatherhub.databinding.ActivityLoginBinding
import com.driverskr.weatherhub.ui.activity.vm.LoginViewModel
import com.driverskr.weatherhub.ui.base.BaseVmActivity
import com.tencent.tauth.DefaultUiListener
import com.tencent.tauth.UiError
import org.json.JSONObject

open class LoginActivity: BaseVmActivity<ActivityLoginBinding, LoginViewModel>() {

    override fun bindView() = ActivityLoginBinding.inflate(layoutInflater)

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

abstract class BaseUiListener : DefaultUiListener() {
    override fun onComplete(response: Any?) {
        if (response == null) {
            logE("LoginActivity","返回为空，登录失败")
            return
        }
        val jsonResponse = response as JSONObject
        if (jsonResponse.length() == 0) {
            logE("LoginActivity","返回为空，登录失败")
            return
        }
        doComplete(response)
    }
    abstract fun doComplete(values: JSONObject)

    override fun onError(e: UiError) {
        Log.e("fund", "onError: ${e.errorDetail}")
    }

    override fun onCancel() {
        logE("LoginActivity","取消登录")
    }
}