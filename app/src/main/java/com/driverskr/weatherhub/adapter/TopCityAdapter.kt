package com.driverskr.weatherhub.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.driverskr.weatherhub.databinding.ItemTopCityBinding

/**
 * @Author: driverSkr
 * @Time: 2023/11/30 10:50
 * @Description: 热门城市推荐列表$
 */
class TopCityAdapter(private val mData: List<String>, val onChecked: (String) -> Unit): RecyclerView.Adapter<TopCityAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTopCityBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mData[position]
        holder.binding.tvCityName.text = item

        holder.itemView.setOnClickListener {
            onChecked(item)
        }
    }

    override fun getItemCount() = mData.size

    class ViewHolder(val binding: ItemTopCityBinding) : RecyclerView.ViewHolder(binding.root)
}