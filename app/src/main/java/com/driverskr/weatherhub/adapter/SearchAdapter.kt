package com.driverskr.weatherhub.adapter

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.driverskr.weatherhub.R
import com.driverskr.weatherhub.bean.CityBean
import com.driverskr.weatherhub.databinding.ItemSearchingBinding

/**
 * @Author: driverSkr
 * @Time: 2023/11/30 9:13
 * @Description: 搜索城市结果适配器$
 */
class SearchAdapter(
    private val mContext: Context,
    private val data: List<CityBean>,
    private val searchText: String,
    private val onCityChecked: (CityBean) -> Unit
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder(
            ItemSearchingBinding.inflate(LayoutInflater.from(mContext), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        val item = data[position]
        val name = item.cityName
        val x = name.indexOf("-")
        val parentCity = name.substring(0,x)
        val location = name.substring(x + 1)
        var cityName = location + "，" + parentCity + "，" + item.adminArea + "，" + item.cnty
        if (TextUtils.isEmpty(item.adminArea)) {
            cityName = location + "，" + parentCity + "，" + item.cnty
        }
        //高亮关键字
        if (!TextUtils.isEmpty(cityName)) {
            viewHolder.binding.tvCity.text = cityName
            if (cityName.contains(searchText)) {
                viewHolder.binding.tvCity.text = matcherSearchText(cityName,searchText)
            }
        }
        //点击添加到数据库
        viewHolder.itemView.setOnClickListener {
            onCityChecked(data[position])
        }
    }

    override fun getItemCount() = data.size

    /**
     * 改变一段文本中第一个关键字的文字颜色
     *
     * @param string  文本字符串
     * @param keyWord 关键字
     * SpannableStringBuilder ，通过这个可以设置一行文字多种颜色
     */
    private fun matcherSearchText(string: String, keyWord: String): CharSequence {
        val buildr = SpannableStringBuilder(string)
        //返回关键字在原始字符串中第一次出现的位置的索引
        val index = string.indexOf(keyWord)
        if (index != -1) {
            buildr.setSpan(ForegroundColorSpan(ContextCompat.getColor(mContext,R.color.light_text_color)),
                index,index + keyWord.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        return buildr
    }

    internal inner class ViewHolder(val binding: ItemSearchingBinding) : RecyclerView.ViewHolder(binding.root)
}