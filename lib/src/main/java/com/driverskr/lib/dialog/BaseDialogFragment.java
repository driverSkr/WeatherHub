package com.driverskr.lib.dialog;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewbinding.ViewBinding;

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 15:18
 * @Description: DialogFragment 是 Android 中的一个特殊类型的 Fragment，用于显示对话框或弹出窗口，并允许你管理对话框的外观和行为。
 *               DialogFragment 主要用于以分离的方式管理对话框，类似于 Fragment 但专注于显示对话框$
 */
public abstract class BaseDialogFragment<T extends ViewBinding> extends DialogFragment implements DialogInit<T> {

    protected T mBinding;

    protected int mGravity = Gravity.CENTER;

    private float widthWeight = 0f;
    private float heightWeight = 0f;

    public BaseDialogFragment() {
        this(Gravity.CENTER);
    }

    /**
     * dialog
     * @param gravity 在屏幕的位置
     */
    public BaseDialogFragment(int gravity) {
        this(gravity, 0f, 0f);
    }

    /**
     * dialog
     */
    public BaseDialogFragment(float widthWeight, float heightWeight) {
        this(Gravity.CENTER, widthWeight, heightWeight);
    }

    public BaseDialogFragment(int gravity, float widthWeight, float heightWeight) {
        mGravity = gravity;
        if (widthWeight <= 1f) {
            this.widthWeight = widthWeight;
        }

        if (heightWeight <= 1f) {
            this.heightWeight = heightWeight;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = bindView();
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initDialog();

        initView();
        initEvent();
    }

    private void initDialog() {
        // 设置宽度为屏宽、位置靠近屏幕底部
        Window window = getDialog().getWindow();
        if (window == null) {
            return;
        }
        WindowManager windowManager = window.getWindowManager();
        //获取默认显示对象，通常是屏幕的显示信息
        Display display = windowManager.getDefaultDisplay();
        //设置对话框的背景为透明。这一行代码的作用是将对话框的背景设为透明，通常用于去掉对话框默认的背景，使得对话框的边缘部分不可见
        window.setBackgroundDrawableResource(android.R.color.transparent);
        //获取窗口的布局参数对象
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = mGravity;
        //获取屏幕的尺寸信息
        Point displaySize = new Point();
        display.getSize(displaySize);

        /**
         * 根据权重设置对话框的宽度。
         * 如果 widthWeight 大于 0，表示设置了权重，将宽度设置为屏幕宽度乘以权重；
         * 否则，将宽度设置为 WRAP_CONTENT，即自适应内容。
         */
        if (widthWeight > 0f) {
            //设置dialog宽度
            wlp.width = (int) (displaySize.x * widthWeight);
        } else {
            wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        if (heightWeight > 0f) {
            wlp.height = (int) (displaySize.y * heightWeight); //设置dialog宽度
        } else {
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        //将设置好的布局参数应用到对话框的窗口中
        window.setAttributes(wlp);
    }
}
