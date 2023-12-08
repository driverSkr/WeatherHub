package com.driverskr.weatherhub.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.driverskr.weatherhub.databinding.ItemCityManagerBinding
import com.driverskr.weatherhub.logic.db.entity.CityEntity
import java.util.*

/**
 * @Author: driverSkr
 * @Time: 2023/11/30 14:47
 * @Description: 城市管理$
 */
class CityManagerAdapter(
    private val mData: List<CityEntity>,
    var onSort: ((List<CityEntity>) -> Unit)? = null
): RecyclerView.Adapter<CityManagerAdapter.ViewHolder>(),  IDragSort {

    var listener: OnCityRemoveListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemCityManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[holder.adapterPosition]
        holder.binding.tvItemCity.text = item.cityName

        holder.binding.tvDelete.setOnClickListener {
            listener?.onCityRemove(holder.adapterPosition)
        }
        if(item.isLocal) {
            holder.binding.ivLocal.visibility = View.VISIBLE
            holder.binding.ivDrag.visibility = View.GONE
        } else {
            holder.binding.ivLocal.visibility = View.GONE
            holder.binding.ivDrag.visibility = View.VISIBLE
        }
        holder.itemView.setOnClickListener {
        }
    }

    override fun getItemCount(): Int = mData.size

    //移动列表中项的位置
    override fun move(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) {
                Collections.swap(mData, i , i - 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(mData, i, i - 1)
            }
        }
        notifyItemMoved(from, to)
    }

    override fun dragFinish() {
        onSort?.let { it(mData) }
    }

    class ViewHolder(val binding: ItemCityManagerBinding) : RecyclerView.ViewHolder(binding.root)

    interface OnCityRemoveListener {
        fun onCityRemove(pos: Int)
    }
}