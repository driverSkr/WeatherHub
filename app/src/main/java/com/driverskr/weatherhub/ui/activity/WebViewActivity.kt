package com.driverskr.weatherhub.ui.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.WindowManager
import android.webkit.*
import com.driverskr.lib.extension.logD
import com.driverskr.lib.extension.toast
import com.driverskr.weatherhub.R
import com.driverskr.weatherhub.databinding.ActivityWebViewBinding
import com.driverskr.weatherhub.ui.base.BaseActivity
import com.driverskr.weatherhub.ui.vassonic.*
import com.driverskr.weatherhub.utils.Constant
import com.driverskr.weatherhub.utils.ShareUtil.showDialogShare
import com.gyf.immersionbar.ImmersionBar
import com.tencent.sonic.sdk.*

class WebViewActivity : BaseActivity<ActivityWebViewBinding>() {

    private var titleName: String = ""  //标题

    private var linkUrl: String = ""    //链接URL

    private var isShare: Boolean = false    //是否允许分享

    private var isTitleFixed: Boolean = false   //是否固定标题

    //用于优化WebView加载速度
    private var sonicSession: SonicSession? = null
    private var sonicSessionClient: SonicSessionClientImpl? = null

    //用于存储加载模式，有默认模式、Sonic模式和Sonic带离线缓存模式。
    private var mode: Int = MODE_DEFAULT

    override fun bindView(): ActivityWebViewBinding = ActivityWebViewBinding.inflate(layoutInflater)

    /**
     * 接收数据
     * @param intent
     */
    override fun prepareData(intent: Intent?) {
        initParams()
        //初始化VasSonic框架，用于提升H5页面加载速度。
        preloadInitVasSonic()
    }

    /**
     * 初始化布局组件
     */
    override fun initView() {
        hideTitleBar()

        //设置状态栏背景色
        ImmersionBar.with(this)
            .autoStatusBarDarkModeEnable(true,0.2f)
            .statusBarColor(R.color.colorPrimaryDarkBar)
            .fitsSystemWindows(true)
            .init()

        //标题旁的返回按钮
        val navigateBefore = mBinding.titleBar.ivNavigateBefore
        //页面的标题
        val tvTitle = mBinding.titleBar.tvTitle
        //返回或导航前一步
        navigateBefore.setOnClickListener { finish() }
        //允许文本在 TextView 控件中水平滚动，以便显示长文本
        tvTitle.isSelected = true  //获取焦点，实现跑马灯效果。

        //表示正在使用 Sonic 模式加载网页
        if (sonicSessionClient != null){
            // 将WebView与SonicSessionClient绑定，用于优化WebView的加载速度
            sonicSessionClient?.bindWebView(mBinding.webView)
            // 表示SonicSessionClient 已经准备就绪，可以开始优化加载
            sonicSessionClient?.clientReady()
        } else {
            // 如果没有使用Sonic 模式，就使用普通的 WebView 加载页面
            mBinding.webView.loadUrl(linkUrl)
        }
    }

    override fun onBackPressed() {
        if (mBinding.webView.canGoBack()) {
            mBinding.webView.goBack()
        } else {
            finish()
        }
    }

    /**
     * 处理事件
     */
    override fun initEvent() {}

    /**
     * 初始化数据
     */
    override fun initData() {
        initTitleBar()
        initWebView()
    }

    override fun onDestroy() {
        mBinding.webView.destroy()
        sonicSession?.destroy()
        sonicSession = null
        mBinding = null
        super.onDestroy()
    }

    /**
     * intent是别的活动要调用这个网页加载活动时，传进来的值，如果自己调用，则传默认值
     */
    private fun initParams() {
        val componentName = intent.component
        val sourceActivity = componentName?.className
        logD(TAG, "intent来源：$sourceActivity")
        //UIWebChromeClient 类的 onReceivedTitle方法中设置的
        titleName = intent.getStringExtra(TITLE) ?: Constant.appName
        linkUrl = intent.getStringExtra(LINK_URL) ?: DEFAULT_URL
        isShare = intent.getBooleanExtra(IS_SHARE,true)
        isTitleFixed = intent.getBooleanExtra(IS_TITLE_FIXED,false)
        mode = intent.getIntExtra(PARAM_MODE, MODE_DEFAULT)
    }

    private fun initTitleBar() {
        //title是在 initParams 方法中初始化的
        mBinding.titleBar.tvTitle.text = titleName
        if (isShare) mBinding.titleBar.ivShare.visibility = View.VISIBLE
        //设置了分享按钮 ivShare 的点击事件监听器
        mBinding.titleBar.ivShare.setOnClickListener {
            showDialogShare(this,"${title}:${linkUrl}")
        }
    }

    //配置 WebView 的属性、客户端和下载监听器，以便 WebView 可以在加载和显示网页时以所需的方式行为
    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        mBinding.webView.settings.run {
            //允许 WebView 加载混合内容，包括 HTTP 和 HTTPS 混合的内容。
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            javaScriptEnabled = true        //启用 JavaScript
            //移除名为 "searchBoxJavaBridge_" 的 JavaScript 接口
            mBinding.webView.removeJavascriptInterface("searchBoxJavaBridge_")
            //向 Intent 添加一个额外的参数，用于指定加载 URL 的时间戳
            intent.putExtra(SonicJavaScriptInterface.PARAM_LOAD_URL_TIME, System.currentTimeMillis())
            //向 WebView 添加一个名为 "sonic" 的 JavaScript 接口，以与 Sonic 框架进行通信
            mBinding.webView.addJavascriptInterface(SonicJavaScriptInterface(sonicSessionClient, intent), "sonic")
            allowContentAccess = true   //允许内容访问
            databaseEnabled = true      //启用数据库支持
            domStorageEnabled = true    //启用 DOM 存储支持
            setAppCacheEnabled(true)    //启用应用缓存
            savePassword = false        //禁用密码保存功能
            saveFormData = false        //禁用表单数据保存功能
            useWideViewPort = true      //启用支持宽视图端口
            loadWithOverviewMode = true //启用以概览模式加载页面
            defaultTextEncodingName = "UTF-8"   //设置默认的文本编码为 UTF-8
            setSupportZoom(true)        //启用支持缩放
        }
        //这个客户端用于处理 WebView 中的 Chrome 客户端事件，例如标题更新
        mBinding.webView.webChromeClient = UIWebChromeClient(mBinding, this)
        //这个客户端用于处理 WebView 的页面加载事件
        mBinding.webView.webViewClient = UIWebViewClient(mBinding, this)
        //设置一个下载监听器，当用户点击下载链接时，将触发该监听器。
        // 在监听器中，将创建一个 Intent 来调用系统浏览器来下载文件
        mBinding.webView.setDownloadListener { url, _, _, _, _ ->
            // 调用系统浏览器下载
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
    }

    /**
     * 使用VasSonic框架提升H5首屏加载速度。
     */
    private fun preloadInitVasSonic() {
        window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)

        // init sonic engine if necessary, or maybe u can do this when application created
        if (!SonicEngine.isGetInstanceAllowed()) {
            SonicEngine.createInstance(SonicRuntimeImpl(application), SonicConfig.Builder().build())
        }

        // if it's sonic mode , startup sonic session at first time
        if (MODE_DEFAULT != mode) { // sonic mode
            val sessionConfigBuilder = SonicSessionConfig.Builder()
            sessionConfigBuilder.setSupportLocalServer(true)

            // if it's offline pkg mode, we need to intercept the session connection
            if (MODE_SONIC_WITH_OFFLINE_CACHE == mode) {
                sessionConfigBuilder.setCacheInterceptor(object : SonicCacheInterceptor(null) {
                    override fun getCacheData(session: SonicSession): String? {
                        return null // offline pkg does not need cache
                    }
                })
                sessionConfigBuilder.setConnectionInterceptor(object : SonicSessionConnectionInterceptor() {
                    override fun getConnection(session: SonicSession, intent: Intent): SonicSessionConnection {
                        return OfflinePkgSessionConnection(this@WebViewActivity, session, intent)
                    }
                })
            }

            // create sonic session and run sonic flow
            sonicSession = SonicEngine.getInstance().createSession(linkUrl, sessionConfigBuilder.build())
            if (null != sonicSession) {
                sonicSession?.bindClient(SonicSessionClientImpl().also { sonicSessionClient = it })
            } else {
                // this only happen when a same sonic session is already running,
                // u can comment following codes to feedback as a default mode.
                // throw new UnknownError("create session fail!");
                logD(TAG, "${title},${linkUrl}:create sonic session fail!")
            }
        }
    }

    //扩展了 Android 的 WebViewClient 类。这个类用于处理 WebView 中页面加载和资源拦截等事件
    class UIWebViewClient(val binding: ActivityWebViewBinding, val activity: WebViewActivity) : WebViewClient() {
        //在页面加载开始时被调用
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            //将 activity 对象中的 linkUrl 成员变量设置为当前页面的 URL，以跟踪当前加载的页面
            activity.linkUrl = url
            super.onPageStarted(view, url, favicon)
            //将加载进度条设置为可见，以通知用户页面加载已经开始
            binding.progressBar.visibility = View.VISIBLE
        }
        //在页面加载完成时被调用
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            //如果存在 SonicSession（Sonic 框架的会话），则调用 SonicSession 的 pageFinish 方法，通知 Sonic 框架当前页面加载已完成
            activity.sonicSession?.sessionClient?.pageFinish(url)
            //将加载进度条设置为不可见，表示页面加载完成
            binding.progressBar.visibility = View.INVISIBLE
        }
        //用于拦截 WebView 中的资源请求，以进行特定处理
        override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
            if (activity.sonicSession != null) {
                //如果存在 SonicSession，则调用请求资源。这可能涉及到 Sonic 框架的资源拦截和处理
                val requestResponse = activity.sonicSessionClient?.requestResource(request?.url.toString())
                //如果成功获取了 WebResourceResponse，则返回该资源响应
                if (requestResponse is WebResourceResponse) return requestResponse
            }
            //执行默认的资源请求拦截操作
            return super.shouldInterceptRequest(view, request)
        }
    }

    //扩展了 Android 的 WebChromeClient 类。
    // 这个类用于处理 WebView 中页面的 Chrome 客户端事件，例如标题更新
    class UIWebChromeClient(val binding: ActivityWebViewBinding, val activity: WebViewActivity) : WebChromeClient() {
        //这个方法在 WebView 接收到页面标题时被调用
        override fun onReceivedTitle(view: WebView?, title: String?) {
            //执行默认的处理，通常是更新活动的标题栏
            super.onReceivedTitle(view, title)
            //检查是否应该固定标题
            if (!activity.isTitleFixed) {
                title?.run {
                    //将当前活动的标题设置为页面的标题，这会导致应用程序的标题栏显示页面的标题
                    activity.title = this
                    //将页面标题设置为标题栏中的文本视图的文本内容，以使用户能够看到页面标题
                    binding.titleBar.tvTitle.text = this
                }
            }
        }
    }

    companion object {

        private const val TAG: String = "WebViewActivity"

        //用于定义 Intent 中的额外数据键（extra keys）
        private const val TITLE = "title"   //标题

        private const val LINK_URL = "link_url" //链接地址

        private const val IS_SHARE = "is_share" //是否分享

        private const val IS_TITLE_FIXED = "isTitleFixed"   //是否固定标题

        //加载模式的键
        const val MODE_DEFAULT = 0  //默认模式

        private const val MODE_SONIC = 1    //Sonic 模式

        const val MODE_SONIC_WITH_OFFLINE_CACHE = 2 //Sonic 带离线缓存模式

        const val PARAM_MODE = "param_mode"

        //定义了一个名为 DEFAULT_URL 的常量，表示默认的链接地址。
        // 如果在启动 WebViewActivity 时未指定链接地址，则将使用该默认链接地址
        const val DEFAULT_URL = "https://www.hihonor.com/cn/shop/product/10086427157985.html?cid=427755"

        //这个标题用于作为应用程序的默认标题，如果在启动 WebViewActivity 时未指定标题
        val DEFAULT_TITLE = Constant.appName

        /**
         * 这是一个静态方法，可通过类名直接调用，用于跳转到WebView网页界面并传递相关参数。
         *
         * @param context       上下文环境
         * @param title         标题
         * @param url           加载地址
         * @param isShare       是否显示分享按钮
         * @param isTitleFixed  是否固定显示标题，不会通过动态加载后的网页标题而改变。true：固定，false 反之。
         * @param mode          加载模式：MODE_DEFAULT 默认使用WebView加载；MODE_SONIC 使用VasSonic框架加载； MODE_SONIC_WITH_OFFLINE_CACHE 使用VasSonic框架离线加载
         */
        fun startActivity(context: Context, title: String = "", url: String = "", isShare: Boolean = true, isTitleFixed: Boolean = true, mode: Int = MODE_SONIC) {
            url.preCreateSession()  //预加载url
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra(TITLE, title)
                putExtra(LINK_URL, url)
                putExtra(IS_SHARE, isShare)
                putExtra(IS_TITLE_FIXED, isTitleFixed)
                putExtra(PARAM_MODE, mode)
                putExtra(SonicJavaScriptInterface.PARAM_CLICK_TIME, System.currentTimeMillis())
            }
            context.startActivity(intent)
        }
    }
}