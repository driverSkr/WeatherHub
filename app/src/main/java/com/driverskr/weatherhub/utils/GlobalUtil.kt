package com.driverskr.weatherhub.utils

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.driverskr.lib.BaseApplication.Companion.context

object GlobalUtil {

    /**
     * 判断某个应用是否安装。
     * @param packageName
     * 要检查是否安装的应用包名
     * @return 安装返回true，否则返回false。
     */
    private fun isInstalled(packageName: String): Boolean{
        val packageInfo: PackageInfo? = try {
            context.packageManager.getPackageInfo(packageName,0)
        } catch (e: PackageManager.NameNotFoundException){
            null
        }
        return packageInfo != null
    }

    /**
     * 判断手机是否安装了QQ。
     */
    fun isQQInstalled() = isInstalled("com.tencent.mobileqq")

    /**
     * 判断手机是否安装了微信。
     */
    fun isWechatInstalled() = isInstalled("com.tencent.mm")

    /**
     * 判断手机是否安装了微博。
     * */
    fun isWeiboInstalled() = isInstalled("com.sina.weibo")
}
