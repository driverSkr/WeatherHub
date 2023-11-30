package com.driverskr.weatherhub.ui.activity

import android.content.Intent
import androidx.recyclerview.widget.ItemTouchHelper
import com.driverskr.weatherhub.R
import com.driverskr.weatherhub.adapter.CityManagerAdapter
import com.driverskr.weatherhub.adapter.MyItemTouchCallback
import com.driverskr.weatherhub.databinding.ActivityCityManagerBinding
import com.driverskr.weatherhub.logic.db.entity.CityEntity
import com.driverskr.weatherhub.ui.activity.vm.CityManagerViewModel
import com.driverskr.weatherhub.ui.base.BaseVmActivity
import com.driverskr.weatherhub.utils.Constant

class CityManagerActivity: BaseVmActivity<ActivityCityManagerBinding, CityManagerViewModel>() {

    private val datas by lazy { ArrayList<CityEntity>() }

    private val dataLocal by lazy { ArrayList<CityEntity>() }

    private var adapterLocal: CityManagerAdapter? = null

    private var adapter: CityManagerAdapter? = null

    private lateinit var itemTouchCallback: MyItemTouchCallback

    override fun bindView() = ActivityCityManagerBinding.inflate(layoutInflater)

    /**
     * 接收数据
     * @param intent
     */
    override fun prepareData(intent: Intent?) {
    }

    /**
     * 初始化布局组件
     */
    override fun initView() {
        setTitle(getString(R.string.control_city))

        itemTouchCallback = MyItemTouchCallback(this)

        adapterLocal = CityManagerAdapter(dataLocal)

        adapter = CityManagerAdapter(datas) {
            viewModel.updateCities(it)
            Constant.CITY_CHANGE = true
        }

        mBinding.rvLocal.adapter = adapterLocal

        mBinding.recycleView.adapter = adapter

        mBinding.recycleView.setStateCallback {
            itemTouchCallback.dragEnable = it
        }

        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(mBinding.recycleView)
    }

    /**
     * 处理事件
     */
    override fun initEvent() {
        adapter?.listener = object : CityManagerAdapter.OnCityRemoveListener {
            override fun onCityRemove(pos: Int) {
                viewModel.removeCity(datas[pos].cityId)
                datas.removeAt(pos)
                adapter?.notifyItemRemoved(pos)
                Constant.CITY_CHANGE = true
            }
        }

        viewModel.cities.observe(this) {
            dataLocal.clear()
            datas.clear()
            for (cityEntity in it) {
                if (cityEntity.isLocal) {
                    dataLocal.add(cityEntity)
                } else {
                    datas.add(cityEntity)
                }
            }
            adapterLocal?.notifyDataSetChanged()
            adapter?.notifyDataSetChanged()
        }
    }

    /**
     * 初始化数据
     */
    override fun initData() {
        viewModel.getCities()
    }
}