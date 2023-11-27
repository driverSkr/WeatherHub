package com.driverskr.weatherhub.ui.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.driverskr.lib.BuildConfig
import com.driverskr.lib.extension.logD
import com.driverskr.lib.extension.logE
import com.driverskr.weatherhub.bean.LoadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 17:55
 * @Description: ViewModel基类$
 */
open class BaseViewModel(app: Application): AndroidViewModel(app) {

    //加载状态
    val loadState = MutableLiveData<LoadState>()

    /**
     * 是否登录
     */
    val isLogin = MutableLiveData<Boolean>()

    private var runningCount = AtomicInteger(0)

    /**
     * 是否正在请求网络
     */
    fun isStopped(): Boolean {
        return runningCount.get() == 0
    }

    /**
     * 后台静默加载，不显示loading
     */
    fun launchSilent(block: suspend CoroutineScope.() -> Unit) {
        launchRequest(1, block)
    }

    /**
     * 开始显示loading,结束关闭loading
     */
    fun launch(block: suspend CoroutineScope.() -> Unit) {
        launchRequest(block = block)
    }

    /**
     * @param loadingType 0: 默认 1: silent
     */
    private fun launchRequest(loadingType: Int = 0, block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch {
            try {
                if (loadingType == 0) {
                    runningCount.getAndIncrement()
                    loadState.value = LoadState.Start()
                }
                withContext(Dispatchers.IO) {
                    block.invoke(this)
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                if (BuildConfig.DEBUG) {
                    logE("BaseViewModel","$loadingType -> 异常：$e")
                    e.printStackTrace()
                }
                if (loadingType == 0) {
                    loadState.value = LoadState.Error(e.message!!)
                    if (runningCount.get() > 0) {
                        runningCount.set(0)
                    }
                    logD("BaseViewModel","runningCount - : $runningCount")
                    loadState.value = LoadState.Finish
                }
            } finally {
                if (loadingType == 0) {
                    if (runningCount.get() > 0) {
                        runningCount.set(0)
                    }
                    loadState.value = LoadState.Finish
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        runningCount.getAndSet(0)
    }
}