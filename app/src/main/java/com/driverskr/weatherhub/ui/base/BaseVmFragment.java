package com.driverskr.weatherhub.ui.base;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.ParameterizedType;

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 18:08
 * @Description: 带ViewModel的fragment$
 */
public abstract class BaseVmFragment<T extends ViewBinding, V extends ViewModel> extends BaseFragment<T> {

    protected V viewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(getViewModelClass());
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * getClass(): 获取当前对象的类。
     * getGenericSuperclass(): 获取当前类的泛型超类，这通常是一个 ParameterizedType，即带有泛型参数的类型。
     * (ParameterizedType): 强制类型转换，将泛型超类转换为 ParameterizedType。
     * getActualTypeArguments(): 获取泛型参数的数组，返回一个 Type[]。
     * [1]: 假设我们的父类有两个泛型参数，这里取得数组的第二个元素，即 [1]。
     */
    public Class<V> getViewModelClass() {
        Class<V> xClass = (Class<V>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        return xClass;
    }
}
