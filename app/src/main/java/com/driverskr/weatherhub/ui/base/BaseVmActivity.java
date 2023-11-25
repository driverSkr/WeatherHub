package com.driverskr.weatherhub.ui.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.ParameterizedType;

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 18:03
 * @Description: 带ViewModel的活动$
 */
public abstract class BaseVmActivity<T extends ViewBinding, V extends ViewModel> extends BaseActivity<T> {

    protected V viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void init() {
        viewModel = new ViewModelProvider(this).get(getViewModelClass());
        super.init();
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

    @Override
    public T bindView() {
        return null;
    }
}
