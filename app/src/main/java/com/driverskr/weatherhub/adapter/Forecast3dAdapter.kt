package com.driverskr.weatherhub.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.driverskr.lib.extension.logD
import com.driverskr.lib.utils.IconUtils
import com.driverskr.lib.utils.WeatherUtil
import com.driverskr.weatherhub.R
import com.driverskr.weatherhub.bean.Daily
import com.driverskr.weatherhub.bean.TempUnit
import com.driverskr.weatherhub.databinding.DialogDailyDetailBinding
import com.driverskr.weatherhub.databinding.ItemForecastBinding
import com.driverskr.weatherhub.utils.Constant
import com.driverskr.weatherhub.utils.EasyDate
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * @Author: driverSkr
 * @Time: 2023/11/29 11:47
 * @Description: 天气预报 3天$
 */
class Forecast3dAdapter(val context: Context, private val data: List<Daily>?):
    RecyclerView.Adapter<Forecast3dAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemForecastBinding.inflate(LayoutInflater.from(context), parent, false)
    )

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data?.get(position)
        item?.let {
            //华氏度和摄氏度之间的切换
            if (Constant.APP_SETTING_UNIT == TempUnit.HUA.tag) {
                val minHua = WeatherUtil.getF(it.tempMin)
                val maxHua = WeatherUtil.getF(it.tempMax)
                holder.binding.tvTemp.text = "${minHua}~${maxHua}°F"
            } else {
                holder.binding.tvTemp.text = "${it.tempMin}~${it.tempMax}°C"
            }

            //天气描述
            var desc = it.textDay
            if (it.textDay != it.textNight) {
                desc += "转" + it.textNight
            }
            holder.binding.tvDesc.text = desc

            //匹配文字和图片
            when (position) {
                0 -> {
                    holder.binding.tvWeek.text = context.getString(R.string.today)
                    if (IconUtils.isDay()) {
                        holder.binding.iv3fDay.setImageResourceName(it.iconDay)
                    } else {
                        holder.binding.iv3fDay.setImageResourceName(it.iconNight)
                    }
                }
                1 -> {
                    holder.binding.tvWeek.text = context.getString(R.string.tomorrow)
                    holder.binding.iv3fDay.setImageResourceName(it.iconDay)
                }
                else -> {
                    holder.binding.tvWeek.text = context.getString(R.string.after_t)
                    holder.binding.iv3fDay.setImageResourceName(it.iconDay)
                }
            }

            holder.itemView.setOnClickListener { showDailyDetailDialog(item) }
        }
    }

    override fun getItemCount(): Int = data?.size ?: 0

    class ViewHolder(val binding: ItemForecastBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * 显示天气预报详情弹窗
     */
    @SuppressLint("SetTextI18n")
    private fun showDailyDetailDialog(daily: Daily) {
        val dialog = BottomSheetDialog(context)
        val detailBinding = DialogDailyDetailBinding.inflate(LayoutInflater.from(context), null, false)
        //关闭弹窗
        detailBinding.ivClose.setOnClickListener { dialog.dismiss() }
        //设置数据显示
        detailBinding.toolbarDaily.title = "${daily.fxDate}  ${EasyDate.getWeek(daily.fxDate)}"
        logD("driverSkr","${daily.fxDate}  ${EasyDate.getWeek(daily.fxDate)}")
        detailBinding.toolbarDaily.subtitle = "天气预报详情"
        detailBinding.tvTmpMax.text = "${daily.tempMax}℃"
        detailBinding.tvTmpMin.text = "${daily.tempMin}℃"
        detailBinding.tvUvIndex.text = daily.uvIndex
        detailBinding.tvCondTxtD.text = daily.textDay
        detailBinding.tvCondTxtN.text = daily.textNight
        detailBinding.tvWindDeg.text = "${daily.wind360Day}°"
        detailBinding.tvWindDir.text = daily.windDirDay
        detailBinding.tvWindSc.text = "${daily.windScaleDay}级"
        detailBinding.tvWindSpd.text = "${daily.windSpeedDay}公里/小时"
        detailBinding.tvCloud.text = "${daily.cloud}%"
        detailBinding.tvHum.text = "${daily.humidity}%"
        detailBinding.tvPres.text = "${daily.pressure}hPa"
        detailBinding.tvPcpn.text = "${daily.precip}mm"
        detailBinding.tvVis.text = "${daily.vis}km"

        dialog.setContentView(detailBinding.root)
        dialog.show()
    }
}