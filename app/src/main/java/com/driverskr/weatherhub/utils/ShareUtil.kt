package com.driverskr.weatherhub.utils

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.driverskr.lib.extension.toast
import com.driverskr.weatherhub.ui.fragment.ShareDialogFragment

/**
 * @Author: driverSkr
 * @Time: 2023/12/5 17:28
 * @Description: 调用系统原生分享工具类。$
 */
const val SHARE_MORE = 0
const val SHARE_QQ = 1
const val SHARE_WECHAT = 2
const val SHARE_WEIBO = 3
const val SHARE_QQZONE = 4
const val SHARE_WECHAT_MEMORIES = 5

object ShareUtil {

    private fun processShare(activity: Activity, shareContent: String, shareType: Int) {
        when (shareType) {
            SHARE_QQ -> {
                if (!GlobalUtil.isQQInstalled()) {
                    activity.toast("您手机还没有安装QQ")
                    return
                }
                share(activity, shareContent, "com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity")
            }
            SHARE_WECHAT -> {
                if (!GlobalUtil.isWechatInstalled()) {
                    activity.toast("您的手机还没有安装微信")
                    return
                }
                share(activity, shareContent, "com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI")
            }
            SHARE_WECHAT_MEMORIES -> {
                if (!GlobalUtil.isWechatInstalled()) {
                    activity.toast("您的手机还没有安装微信")
                    return
                }
                share(activity, shareContent, "com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI")
            }
            SHARE_WEIBO -> {
                if (!GlobalUtil.isWeiboInstalled()) {
                    activity.toast("您的手机还没安装微博")
                    return
                }
                share(activity, shareContent, "com.sina.weibo", "com.sina.weibo.composerinde.ComposerDispatchActivity")
            }
            SHARE_QQZONE -> {
                if (!GlobalUtil.isQZoneInstalled()) {
                    activity.toast("您的手机还没安装QQ空间")
                    return
                }
                share(activity, shareContent, "com.qzone", "com.qzonex.module.operation.ui.QZonePublishMoodActivity")
            }
            SHARE_MORE -> {
                share(activity, shareContent)
            }
        }
    }

    private fun share(activity: Activity, shareContent: String, packageName: String, className: String) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareContent)
                setClassName(packageName, className)
            }
            activity.startActivity(shareIntent)
        } catch (e: Exception) {
            activity.toast("分享出现未知异常")
        }
    }

    private fun share(activity: Activity,shareContent: String){
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT,shareContent)
        }
        activity.startActivity(Intent.createChooser(shareIntent,"分享到"))
    }

    /**
     * 调用系统原生分享
     *
     * @param shareContent 分享内容
     * @param shareType SHARE_MORE=0，SHARE_QQ=1，SHARE_WECHAT=2，SHARE_WEIBO=3，SHARE_QQZONE=4
     */
    fun share(activity: Activity,shareContent: String,shareType: Int){
        processShare(activity,shareContent,shareType)
    }

    /**
     * 弹出分享对话框。
     *
     * @param activity 上下文
     * @param shareContent 分享内容
     */
    fun showDialogShare(activity: Activity,shareContent: String){
        if (activity is AppCompatActivity){
            ShareDialogFragment().showDialog(activity, shareContent)
        }
    }
}