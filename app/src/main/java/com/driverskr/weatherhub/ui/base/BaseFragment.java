package com.driverskr.weatherhub.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 17:49
 * @Description: fragment基类$
 */
public abstract class BaseFragment<T extends ViewBinding> extends Fragment {

    protected Activity mContext;
    private Boolean isLoaded = false;
    protected T mBinding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
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

        initView(view);
        initEvent();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoaded) {
            isLoaded = true;
            loadData();
        }
    }

    protected abstract T bindView();

    public abstract void initView(View view);

    public abstract void initEvent();

    /**
     * 数据初始化，只会执行一次
     */
    public abstract void loadData();

    @Override
    public void onDestroy() {
        super.onDestroy();
        isLoaded = false;
    }
}
