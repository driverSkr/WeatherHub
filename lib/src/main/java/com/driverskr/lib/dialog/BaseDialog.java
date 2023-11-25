package com.driverskr.lib.dialog;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialog;
import androidx.viewbinding.ViewBinding;

import com.driverskr.lib.R;

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 14:42
 * @Description: dialog$
 */
public abstract class BaseDialog<T extends ViewBinding> extends AppCompatDialog implements DialogInit<T> {

    //对话框的显示位置，默认是居中
    private int mGravity = Gravity.CENTER;

    //对话框的宽度和高度的权重，用于设置对话框宽度和高度相对于屏幕的比例
    private float widthWeight = 0f;
    private float heightWeight = 0f;

    protected T mBinding;

    public BaseDialog(@NonNull Context context) {
        this(context, 0, 0);
    }

    public BaseDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected BaseDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    /**
     * 自定义的构造函数
     */
    public BaseDialog(@NonNull Context context, float widthWeight, float heightWeight) {
        this(context, Gravity.CENTER, widthWeight, heightWeight);
    }
    public BaseDialog(Context context, int gravity, float widthWeight, float heightWeight) {
        super(context, R.style.BaseDialogTheme);
        //初始化对话框的一些参数，包括视图绑定、对话框的宽度和高度权重等
        init(gravity, widthWeight, heightWeight);
    }

    /**
     * 用于在对话框显示之前进行一些初始化
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //用于配置对话框的样式，包括宽度、高度等
        initDialog();
        initView();
        // 必须放在这里,不然通过构造方法传过去的之在该方法之后接收到
        initEvent();
    }

    /**
     * 初始化
     */
    private void init(int gravity, float widthWeight, float heightWeight) {
        mBinding = bindView();

        setContentView(mBinding.getRoot());

        mGravity = gravity;

        if (widthWeight <= 1f) {
            this.widthWeight = widthWeight;
        }

        if (heightWeight <= 1f) {
            this.heightWeight = heightWeight;
        }
    }

    /**
     * dialog初始化
     */
    private void initDialog() {
        // 设置宽度为屏宽、位置靠近屏幕底部
        //获取对话框的窗口对象
        Window window = getWindow();
        if (window == null) {
            return;
        }
        WindowManager windowManager = window.getWindowManager();
        //获取默认显示对象，通常是屏幕的显示信息
        Display display = windowManager.getDefaultDisplay();
        /**获取窗口的布局参数对象**/
        WindowManager.LayoutParams attributes = window.getAttributes();
        //设置对话框的显示位置
        attributes.gravity = mGravity;
        //获取屏幕的尺寸信息
        Point displaySize = new Point();
        display.getSize(displaySize);

        /**
         * 根据权重设置对话框的宽度。
         * 如果 widthWeight 大于 0，表示设置了权重，将宽度设置为屏幕宽度乘以权重；
         * 否则，将宽度设置为 WRAP_CONTENT，即自适应内容
         */
        if (widthWeight > 0f) {
            //设置dialog宽度
            attributes.width = (int) (displaySize.x * widthWeight);
        } else {
            attributes.width = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        if (heightWeight > 0f) {
            //设置dialog宽度
            attributes.height = (int) (displaySize.y * heightWeight);
        } else {
            attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }

        //将设置好的布局参数应用到对话框的窗口中
        window.setAttributes(attributes);
        //为了触发对话框的根视图重新绘制
        mBinding.getRoot().postInvalidate();
    }
}
