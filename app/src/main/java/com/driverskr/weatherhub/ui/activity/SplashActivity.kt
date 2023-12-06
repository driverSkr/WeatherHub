package com.driverskr.weatherhub.ui.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.view.ViewPropertyAnimator
import androidx.lifecycle.lifecycleScope
import com.driverskr.lib.extension.logD
import com.driverskr.lib.extension.startActivity
import com.driverskr.weatherhub.databinding.ActivitySplashBinding
import com.driverskr.weatherhub.logic.DBRepository
import com.driverskr.weatherhub.service.WidgetService
import com.driverskr.weatherhub.ui.base.BaseActivity
import com.driverskr.weatherhub.utils.Constant
import com.driverskr.weatherhub.utils.DisplayUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("CustomSplashScreen","ObsoleteSdkInt")
class SplashActivity: BaseActivity<ActivitySplashBinding>() {

    private lateinit var animate: ViewPropertyAnimator

    private fun startIntent() {
        lifecycleScope.launch {
            var citySize: Int

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //startForegroundService(Intent(this@SplashActivity, WidgetService::class.java))
                logD("SplashActivity","startService")
            } else {
                startService(Intent(this@SplashActivity, WidgetService::class.java))
                logD("SplashActivity","startService")
            }

            withContext(Dispatchers.IO) {
                val cities = DBRepository.getInstance().getCities()
                citySize = cities.size
                getScreenInfo()

                delay(1200L)
            }
            if (citySize == 0) {
                AddCityActivity.startActivity(this@SplashActivity, true)
            } else {
                startActivity<HomeActivity>()
            }
            finish()
        }
    }

    private fun getScreenInfo() {
        val screenRealSize = DisplayUtil.getScreenRealSize(this@SplashActivity).y
        val navHeight = if (DisplayUtil.isNavigationBarShowing(this@SplashActivity))
                            DisplayUtil.getNavigationBarHeight(this@SplashActivity)
                        else 0
        val statusBarHeight = DisplayUtil.getStatusBarHeight2(this@SplashActivity)
        val dp45 = DisplayUtil.dp2px(45f)
        Constant.screenHeight = screenRealSize
        Constant.visibleHeight = screenRealSize - navHeight - statusBarHeight - dp45
    }

    override fun bindView() = ActivitySplashBinding.inflate(layoutInflater)

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
        hideTitleBar()
        immersionStatusBar()

        animate = mBinding.ivLogo.animate()

        animate.apply {
            duration = 1200L
            translationYBy(-80F)
            scaleXBy(0.2F)
            scaleYBy(0.2f)
        }
        startIntent()
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

    override fun onDestroy() {
        super.onDestroy()
        animate.cancel()
    }
}