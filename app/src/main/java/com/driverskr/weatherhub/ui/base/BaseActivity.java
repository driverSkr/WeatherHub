package com.driverskr.weatherhub.ui.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;

import com.driverskr.lib.dialog.LoadingDialog;
import com.driverskr.weatherhub.R;

import java.util.Objects;

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 14:38
 * @Description: activity基类$
 */
public abstract class BaseActivity<T extends ViewBinding> extends AppCompatActivity implements CreateInit<T> {

    private FrameLayout layout_content; // 子类view容器

    protected Context context;

    private LoadingDialog loadingDialog;

    protected T mBinding;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.setContentView(R.layout.activity_base);

        context = this;
        init();
    }

    protected void init() {
        Toolbar toolbar = super.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        layout_content = super.findViewById(R.id.frameLayout);

        mBinding = bindView();
        setContentView(mBinding.getRoot());

        // 返回键显示默认图标
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        //返回键可用
        getSupportActionBar().setHomeButtonEnabled(true);

        // 初始化数据
        prepareData(getIntent());
        // 初始化view
        initView();
        // 初始化事件
        initEvent();
        // 加载数据
        initData();
    }

    /**
     * 用于处理在活动（Activity）已经存在且位于前台时，收到新的 Intent 的情况。
     * 这种情况通常发生在活动已经启动并且用户再次点击应用程序图标或者从通知栏启动应用时。
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // 初始化数据
        prepareData(intent);
    }

    public void hideTitleBar() {
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    // 设置标题
    public void setTitle(String title) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(title);
    }

    public void setContentView(View view) {
        layout_content.removeAllViews();
        layout_content.addView(view);
    }

    /**
     * 沉浸式状态栏
     */
    @SuppressLint("ObsoleteSdkInt")
    protected void immersionStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 沉浸式状态栏
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            // 状态栏改为透明
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void showLoading(boolean show) {
        showLoading(show, null);
    }

    protected void showLoading(boolean show, String tip) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        if (show) {
            if (tip != null && !tip.isEmpty()) {
                loadingDialog.setTitle(tip);
            } else {
                loadingDialog.setTitle("请稍后...");
            }
            loadingDialog.show();
        } else {
            loadingDialog.dismiss();
        }
    }
}
