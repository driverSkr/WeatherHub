package com.driverskr.weatherhub.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.driverskr.weatherhub.bean.Daily
import com.driverskr.weatherhub.databinding.ItemForecast7Binding
import java.util.*

/**
 * @Author: driverSkr
 * @Time: 2023/11/29 16:21
 * @Description: 天气预报 7天$
 */
class Forecast7dAdapter(val context: Context, private val datas: List<Daily>):
    RecyclerView.Adapter<Forecast7dAdapter.ViewHolder>() {

    private var mMin = 0
    private var mMax = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemForecast7Binding.inflate(LayoutInflater.from(context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = datas[position]
        holder.binding.apply {
            tvWeek.text = getWeekDay(position)
            tvDate.text = item.fxDate.removeRange(IntRange(0, 4))
            tvDayDesc.text = item.textDay

            ivDay.setImageResourceName(item.iconDay)
            ivNight.setImageResourceName(item.iconNight)

            tvNightDesc.text = item.textNight
            tvWind.text = item.windDirDay
            (item.windScaleDay + "级").also { tvWindScale.text = it }

            tempChart.setData(
                mMin,
                mMax,
                if (position == 0) null else datas[position - 1],
                item,
                if (position == datas.size - 1) null else datas[position + 1]
            )
        }
    }

    override fun getItemCount(): Int = datas.size

    private val weeks = arrayOf("周日", "周一", "周二", "周三", "周四", "周五", "周六")

    private fun getWeekDay(position: Int): String {
        return if (position == 0) {
            "今天"
        } else {
            val calendar = Calendar.getInstance()
            val dateArray = datas[position].fxDate.split("-")
            calendar.set(dateArray[0].toInt(), dateArray[1].toInt() - 1, dateArray[2].toInt())
            var w = calendar.get(Calendar.DAY_OF_WEEK) - 1
            if (w < 0) {
                w = 0
            }
            weeks[w]
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setRange(min: Int, max: Int) {
        mMin = min
        mMax = max
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemForecast7Binding) : RecyclerView.ViewHolder(binding.root)
}