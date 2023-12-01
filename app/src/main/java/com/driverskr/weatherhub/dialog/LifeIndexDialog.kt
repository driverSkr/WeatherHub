package com.driverskr.weatherhub.dialog

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import com.driverskr.weatherhub.R

/**
 * @Author: driverSkr
 * @Time: 2023/12/1 15:03
 * @Description: 生活指数弹窗$
 */
class LifeIndexDialog(context: Context) {

    private val mLifeIndexDialog: LifeIndexDialog
    private var mPopupWindow: PopupWindow? = null
    private val inflater: LayoutInflater
    private val mContext: Context

    init {
        this.mContext = context
        inflater = LayoutInflater.from(context)
        mLifeIndexDialog = this
    }

    /**
     * 中间显示
     * @param mView 弹窗
     */
    fun showCenterPopupWindow(mView: View, width: Int, height: Int, focusable: Boolean) {
        mPopupWindow = PopupWindow(mView, width, height, focusable)
        mPopupWindow?.let {
            it.apply {
                contentView = mView
                //设置动画
                animationStyle = R.style.AnimationCenterFade
                showAtLocation(mView, Gravity.CENTER, 0, 0)
                update()
                setOnDismissListener(closeDismiss)
            }
        }
        setBackgroundAlpha(0.5f, mContext)
        val normal = (mContext as Activity).window.attributes
        normal.alpha = 0.5f
        mContext.window.attributes = normal
    }

    /**
     * 设置弹窗动画
     */
    fun setAnim(animId: Int): LifeIndexDialog {
        mPopupWindow?.let {
            it.animationStyle = animId
        }
        return mLifeIndexDialog
    }

    private val closeDismiss = PopupWindow.OnDismissListener {
        val normal = (mContext as Activity).window.attributes
        normal.alpha = 1f
        mContext.window.attributes = normal
    }

    companion object {
        fun setBackgroundAlpha(bgAlpha: Float, mContext: Context) {
            val lp = (mContext as Activity).window.attributes
            lp.alpha = bgAlpha
            mContext.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            mContext.window.attributes = lp
        }
    }
}