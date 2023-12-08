package com.driverskr.weatherhub.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.graphics.drawable.TransitionDrawable
import android.location.LocationManager
import android.os.Build
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.app.ServiceCompat
import androidx.core.app.ServiceCompat.stopForeground

import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager.widget.ViewPager
import coil.imageLoader
import coil.load
import coil.transform.CircleCropTransformation
import com.baidu.location.BDLocation
import com.driverskr.lib.extension.*
import com.driverskr.lib.utils.EffectUtil
import com.driverskr.lib.utils.IconUtils
import com.driverskr.lib.utils.SpUtil
import com.driverskr.weatherhub.R
import com.driverskr.weatherhub.adapter.ViewPagerAdapter
import com.driverskr.weatherhub.bean.LoadState
import com.driverskr.weatherhub.bean.TempUnit
import com.driverskr.weatherhub.bean.UserInfoBean
import com.driverskr.weatherhub.databinding.ActivityHomeBinding
import com.driverskr.weatherhub.databinding.NavHeaderMainBinding
import com.driverskr.weatherhub.dialog.ChangeCityDialog
import com.driverskr.weatherhub.dialog.UpgradeDialog
import com.driverskr.weatherhub.location.LocationCallback
import com.driverskr.weatherhub.location.WeatherHubLocation
import com.driverskr.weatherhub.logic.DBRepository
import com.driverskr.weatherhub.logic.db.entity.CityEntity
import com.driverskr.weatherhub.service.WidgetService
import com.driverskr.weatherhub.ui.activity.vm.HomeViewModel
import com.driverskr.weatherhub.ui.activity.vm.LAST_LOCATION
import com.driverskr.weatherhub.ui.activity.vm.LoginViewModel
import com.driverskr.weatherhub.ui.activity.vm.SearchViewModel
import com.driverskr.weatherhub.ui.base.BaseVmActivity
import com.driverskr.weatherhub.ui.fragment.WeatherFragment
import com.driverskr.weatherhub.utils.*
import com.driverskr.weatherhub.utils.Constant.appName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeActivity: BaseVmActivity<ActivityHomeBinding, HomeViewModel>(), LocationCallback {

    //权限数组
    private val permissions = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private lateinit var permissionUtils: PermissionUtils
    //定位封装
    private lateinit var weatherHubLocation: WeatherHubLocation
/****************************定位与权限申请分割线***********************************/
    /**fragments 属性在第一次访问时会创建一个新的 ArrayList 实例，之后每次访问都将返回相同的实例
     * ArrayList是MutableList的子类**/
    private val fragments: MutableList<Fragment> by lazy { ArrayList() }
    private val cityList = ArrayList<CityEntity>()
    private var mCurIndex = 0
    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var navHeaderBinding: NavHeaderMainBinding
    private var locationViewModel: SearchViewModel? = null
    private var foregroundCheck: CheckBox? = null //开启状态栏组件的按钮

    //用于跟踪上次按下返回按钮的时间，以实现双击返回按钮退出应用的功能
    private var backPressTime = 0L

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            //此处是跳转的result回调方法
            if (it.data != null && it.resultCode == Activity.RESULT_OK) {
                val isLogin = it.data!!.getBooleanExtra("login", false)
                if (isLogin) {
                    loginViewModel.register(it.data!!.getSerializableExtra("user_info") as UserInfoBean)
                    toast("登录成功")
                } else {
                    toast("已退出登录")
                }
                initUserInfo()
            }
        }

    /**
     * 当前的天气code
     */
    private var currentCode = ""

    override fun bindView() = ActivityHomeBinding.inflate(layoutInflater)

    /**
     * 接收数据
     * @param intent
     */
    override fun prepareData(intent: Intent?) {
        //初始化定位
        weatherHubLocation = WeatherHubLocation.getInstance(this)
        weatherHubLocation.setCallback(this)
        //请求定位权限
        permissionUtils = PermissionUtils(this)
        permissionUtils.requestPermissions(permissions) { granted ->
            if (granted) {
                // 权限已经获取到，开始定位
                weatherHubLocation.startLocation()
            }
        }
    }

    /**
     * 初始化布局组件
     */
    override fun initView() {
        hideTitleBar()
        // 沉浸式态栏
        immersionStatusBar()

        //翻页视图的初始化
        mBinding.viewPager.adapter = ViewPagerAdapter(supportFragmentManager, fragments)
        mBinding.viewPager.offscreenPageLimit = 5
        mBinding.viewPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                mBinding.ivLoc.visibility =
                    if (cityList[position].isLocal) View.VISIBLE else View.INVISIBLE
                mBinding.llRound.getChildAt(mCurIndex).isEnabled = false
                mBinding.llRound.getChildAt(position).isEnabled = true
                mCurIndex = position
                mBinding.tvLocation.text = cityList[position].cityName
            }
        })
        //“添加城市”按钮
        mBinding.ivAddCity.expand(10,10)
        //设置背景图片
        mBinding.ivBg.setImageResource(IconUtils.defaultBg)

        navHeaderBinding = NavHeaderMainBinding.bind(mBinding.navView.getHeaderView(0))
        // 侧边栏顶部下移状态栏高度
        ViewCompat.setOnApplyWindowInsetsListener(navHeaderBinding.llUserHeader) { view, insets ->
            val params = view.layoutParams as LinearLayout.LayoutParams
            params.topMargin = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            insets
        }

        // 设置默认单位
        val unitConfig = PreferenceManager.getDefaultSharedPreferences(context)
            .getString("unit", TempUnit.SHE.tag)
        // 是否开启状态栏组件
        val foregroundState = PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean("foreground_checkout", false)
        val menu = mBinding.navView.menu
        foregroundCheck = menu.findItem(R.id.navForeground).actionView.findViewById(R.id.checkBox)
        foregroundCheck?.isChecked = foregroundState
        if (unitConfig == "she") {
            menu.findItem(R.id.navShe).isChecked = true
        } else {
            menu.findItem(R.id.navHua).isChecked = true
        }
        menu.findItem(R.id.itemUnit).subMenu.setGroupCheckable(R.id.navUnitGroup, true, true)

        //用户信息
        initUserInfo()
    }

    /**
     * 处理事件
     */
    override fun initEvent() {
        mBinding.ivSetting.setOnClickListener {
            if (!mBinding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
                mBinding.drawerLayout.openDrawer(GravityCompat.END)
            }
        }
        mBinding.ivAddCity.setOnClickListener {
            startActivity<AddCityActivity>()
        }
        mBinding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navCity -> {
                    startActivity<CityManagerActivity>()
                }
                R.id.navTheme -> {
                    startActivity<ThemeActivity>()
                }
                R.id.navForeground -> {
                    if (foregroundCheck?.isChecked == true) {
                        foregroundCheck?.isChecked = false
                        viewModel.changeForeground(false)
                        closeForegroundService()
                        toast("你关闭了前台服务")
                    } else {
                        foregroundCheck?.isChecked = true
                        viewModel.changeForeground(true)
                        openForegroundService()
                        toast("你打开了前台服务")
                    }
                }
                R.id.navShe -> {
                    changeUnit(TempUnit.SHE)
                    mBinding.drawerLayout.closeDrawer(GravityCompat.END)
                }
                R.id.navHua -> {
                    changeUnit(TempUnit.HUA)
                    mBinding.drawerLayout.closeDrawer(GravityCompat.END)
                }
                R.id.navFeedback -> {
                    startActivity<FeedBackActivity>()
                }
                R.id.navAbout -> {
                    startActivity<AboutActivity>()
                }
                R.id.navShare -> {
                    ShareUtil.showDialogShare(this, "${title}:我来自天气通，后续我会给你带来更好的分享体验！")
                }
            }
            true
        }

        navHeaderBinding.llUserHeader.setOnClickListener {
            if (SpUtil.getInstance(this).account.isEmpty()) {
                launcher.launch(Intent(this, LoginActivity::class.java))
            } else {
                launcher.launch(Intent(this, UserInfoActivity::class.java))
            }
        }

        // 检查登录状态
        loginViewModel.checkLogin().observe(this) {
            if (!it && SpUtil.getInstance(this).account.isNotEmpty()) {
                TencentUtil.sTencent.logout(this)
                SpUtil.getInstance(this).logout()
            }
        }

        /**监听数据库城市数据**/
        viewModel.mCities.observe(this) {
            if (it.isEmpty()) {
                startActivity<AddCityActivity>()
            } else {
                cityList.clear()
                cityList.addAll(it)
                showCity()
            }
        }

        /**检查是否有新版本**/
        viewModel.newVersion.observe(this) {
            UpgradeDialog(it).show(supportFragmentManager, "upgrade_dialog")
        }

        /**根据天气数据更新背景**/
        viewModel.mCurCondCode.observe(this, ::changeBg)

        getLocation()
    }

    /**
     * 初始化数据
     */
    override fun initData() {
        viewModel.getCities()
        viewModel.checkVersion()
    }

    override fun onResume() {
        super.onResume()
        if (Constant.CITY_CHANGE) {
            viewModel.getCities()
            Constant.CITY_CHANGE = false
        }
        mBinding.ivEffect.drawable?.let {
            (it as Animatable).start()
        }
    }

    override fun onStop() {
        super.onStop()
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.END)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.ivEffect.drawable?.let {
            (it as Animatable).stop()
        }
    }

    /**
     * 接收定位
     * @param bdLocation 定位数据
     */
    override fun onReceiveLocation(bdLocation: BDLocation) {
        val district = bdLocation.district //获取区县
        logD("boge", "定位地址：$district")
        mBinding.tvLocation.text = district
        locationViewModel?.curLocation?.value = district
        locationViewModel?.loadState?.value = LoadState.Finish
    }

    private fun initUserInfo() {
        val account = SpUtil.getInstance(this).account
        if (account.isNotEmpty()) {
            navHeaderBinding.tvAccount.text = account
            navHeaderBinding.ivAvatar.load(
                SpUtil.getInstance(this).avatar, imageLoader = context.imageLoader
            ) {
                placeholder(R.drawable.ic_avatar_default)
                transformations(CircleCropTransformation())
            }
        } else {
            navHeaderBinding.tvAccount.text = getString(R.string.login_plz)
            navHeaderBinding.ivAvatar.load(R.drawable.ic_avatar_default)
        }
    }

    private fun changeUnit(unit: TempUnit) {
        viewModel.changeUnit(unit)
        (fragments[mCurIndex] as WeatherFragment).changeUnit()
    }

    /**
     * 显示城市
     */
    private fun showCity() {
        if (mCurIndex > cityList.size - 1) {
            mCurIndex = cityList.size - 1
        }

        mBinding.ivLoc.visibility =
            if (cityList[mCurIndex].isLocal) View.VISIBLE else View.INVISIBLE
        mBinding.tvLocation.text = cityList[mCurIndex].cityName

        mBinding.llRound.removeAllViews()

        // 宽高参数
        val size = DisplayUtil.dp2px(4f)
        val layoutParams = LinearLayout.LayoutParams(size, size)
        // 设置间隔
        layoutParams.rightMargin = 12

        for (i in cityList.indices) {
            // 创建底部指示器(小圆点)
            val view = View(this@HomeActivity)
            view.setBackgroundResource(R.drawable.background)
            view.isEnabled = false

            // 添加到LinearLayout
            mBinding.llRound.addView(view, layoutParams)
        }
        // 小白点
        mBinding.llRound.getChildAt(mCurIndex).isEnabled = true
        mBinding.llRound.visibility = if (cityList.size <= 1) View.GONE else View.VISIBLE

        fragments.clear()
        for (city in cityList) {
            val cityId = city.cityId
            val weatherFragment = WeatherFragment.newInstance(cityId)
            fragments.add(weatherFragment)
        }

        mBinding.viewPager.adapter = ViewPagerAdapter(supportFragmentManager, fragments)
        mBinding.viewPager.currentItem = mCurIndex
    }

    private fun changeBg(condCode: String) {
        if (currentCode == condCode) {
            return
        }
        currentCode = condCode
        // 获取背景
        val bgDrawable = IconUtils.getBg(this@HomeActivity, condCode.toInt())

        val originDrawable = mBinding.ivBg.drawable
        val targetDrawable = resources.getDrawable(bgDrawable)
        val transitionDrawable = TransitionDrawable(
            arrayOf<Drawable>(
                originDrawable,
                targetDrawable
            )
        )

        mBinding.ivBg.setImageDrawable(transitionDrawable)
        transitionDrawable.isCrossFadeEnabled = true
        transitionDrawable.startTransition(1000)

        // 获取特效
        val effectDrawable = EffectUtil.getEffect(context, condCode.toInt())
        mBinding.ivEffect.setImageDrawable(effectDrawable)
    }

    /**
     * 获取当前城市
     */
    private fun getLocation() {
        if (checkGPSAndPermission()) {
            locationViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
            locationViewModel?.curLocation?.observe(this) {
                if (!it.isNullOrEmpty()) {
                    judgeLocation(it)
                }
            }
            // 根据定位城市获取详细信息
            locationViewModel?.curCity?.observe(this) { item ->
                val curCity = AddCityActivity.location2CityBean(item)
                locationViewModel?.addCity(curCity, isLocal = true, fromSplash = true)
            }
            locationViewModel?.addFinish?.observe(this) {
                viewModel.getCities()
                Constant.CITY_CHANGE = false
            }
        }
    }

    /**
     * 判断城市变化
     */
    private fun judgeLocation(cityName: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val cacheLocation = DBRepository.getInstance().getCache<String>(LAST_LOCATION)
            logE(TAG,"location: $cityName")
            logE(TAG,"cacheLocation: $cacheLocation")
            withContext(Dispatchers.Main) {
                if (cityName != cacheLocation) {
                    ChangeCityDialog(this@HomeActivity).apply {
                        setContent("检测到当前城市为${cityName}，是否切换到该城市")
                        setOnConfirmListener {
                            locationViewModel?.getCityInfo(cityName, true)
                        }
                        show()
                    }
                }
            }
        }
    }

    /**
     * 检查GPS状态及GPS权限
     */
    private fun checkGPSAndPermission(): Boolean {
        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val pr1 = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val pr2 = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if ((pr1 || pr2)) {
            val pm1 = permissionUtils.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
            val pm2 = permissionUtils.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            return (pm1 || pm2)
        }
        return false
    }

    /**
     * 当用户点击返回按钮时，如果抽屉布局是打开的，那么就关闭它，否则就执行processBackPressed()
     */
    override fun onBackPressed() {
        if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            processBackPressed()
        }
    }

    /**
     * 如果距离上一次点击返回按钮（即backPressTime）的时间超过2000毫秒（即2秒），那么就会弹出一个Toast提示用户再次点击返回按钮以退出应用。然后，将当前时间赋值给backPressTime。
     * 如果距离上一次点击返回按钮的时间不到2秒，那么就调用super.onBackPressed()，这通常会导致Activity被销毁，返回上一个Activity。
     */
    private fun processBackPressed() {
        val now = System.currentTimeMillis()
        if (now - backPressTime > 2000) {
            toast(getString(R.string.press_again_to_exit) + appName)
            backPressTime = now
        } else {
            super.onBackPressed()
        }
    }

    /**
     * 开启前台服务
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun openForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this@HomeActivity, WidgetService::class.java))
            logD(TAG,"startService")
            toast("已打开通知栏组件！")
        } else {
            startService(Intent(this@HomeActivity, WidgetService::class.java))
            logD(TAG,"startService")
        }
    }

    /**
     * 关闭前台服务
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun closeForegroundService() {
        val serviceIntent = Intent(this@HomeActivity, WidgetService::class.java)
        // 停止服务
        stopService(serviceIntent)
    }

    companion object {
        private const val TAG = "HomeActivity"
    }
}