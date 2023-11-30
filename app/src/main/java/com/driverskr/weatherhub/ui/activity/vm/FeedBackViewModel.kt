package com.driverskr.weatherhub.ui.activity.vm

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.driverskr.weatherhub.logic.NetworkRepository
import com.driverskr.weatherhub.ui.base.BaseViewModel

/**
 * @Author: driverSkr
 * @Time: 2023/11/30 17:33
 * @Description: $
 */
class FeedBackViewModel(val app: Application): BaseViewModel(app) {

    val feedBackResult = MutableLiveData<String>()

    fun sendFeedBack(content: String) {
        launch {
            val result = NetworkRepository.getInstance().feedBack(content)
            result.let {
                feedBackResult.postValue(it)
            }
        }
    }
}