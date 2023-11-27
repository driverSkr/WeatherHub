package com.driverskr.weatherhub.dialog

import android.content.Context
import com.driverskr.lib.dialog.BaseDialog
import com.driverskr.weatherhub.databinding.DialogChangeCityBinding

/**
 * @Author: driverSkr
 * @Time: 2023/11/27 15:58
 * @Description: 定位城市改变弹窗$
 */
class ChangeCityDialog(context: Context): BaseDialog<DialogChangeCityBinding>(context,0.66f, 0f) {

    /**函数变量**/
    private var mListener: (() -> Unit)? = null

    override fun bindView() = DialogChangeCityBinding.inflate(layoutInflater)

    /**
     * 初始化布局组件
     */
    override fun initView() {
        setCanceledOnTouchOutside(true)
    }

    fun setContent(content: String) {
        mBinding.tvContent.text = content
    }

    /**
     * 增加按钮点击事件
     */
    override fun initEvent() {
        mBinding.tvCancel.setOnClickListener { dismiss() }
        mBinding.tvConfirm.setOnClickListener {
            mListener?.invoke()
            dismiss()
        }
    }

    /**
     * 设置点击“确认”时应该执行什么方法
     */
    fun setOnConfirmListener(listener: () -> Unit) {
        mListener = listener
    }
}