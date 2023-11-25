package com.driverskr.weatherhub.logic.network.api

import com.driverskr.weatherhub.bean.*
import com.driverskr.weatherhub.utils.Constant.HEFENG_KEY
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 10:57
 * @Description: 天气api$
 */
interface WeatherService {

    /**
     * 实况天气
     * @param location  城市ID
     * @return  返回实况天气数据 NowResponse
     */
    @GET("/v7/weather/now?key=$HEFENG_KEY")
    suspend fun nowWeather(@Query("location") location: String): WeatherNow

    /**
     * 天气预报  (免费订阅)最多可以获得7天的数据
     * @param location 城市ID
     * @return 返回天气预报数据 DailyResponse
     */
    @GET("/v7/weather/7d?key=$HEFENG_KEY")
    suspend fun dailyWeather(@Query("location") location: String): ForestBean

    /**
     * 生活指数
     *
     * @param type     可以控制定向获取那几项数据 全部数据 0, 运动指数	1 ，洗车指数	2 ，穿衣指数	3 ，
     *                 钓鱼指数	4 ，紫外线指数  5 ，旅游指数  6，花粉过敏指数	7，舒适度指数	8，
     *                 感冒指数	9 ，空气污染扩散条件指数	10 ，空调开启指数	 11 ，太阳镜指数	12 ，
     *                 化妆指数  13 ，晾晒指数  14 ，交通指数  15 ，防晒指数	16
     * @param location 城市id
     * @return LifestyleResponse 生活指数数据返回
     */
    @GET("/v7/indices/1d?key=$HEFENG_KEY")
    suspend fun lifestyle(@Query("type") type: String = "1,2,3,5,9,14",@Query("location") location: String): LifeIndicator

    /**
     * 逐小时天气
     */
    @GET("/v7/weather/24h?key=$HEFENG_KEY")
    suspend fun hourlyWeather(@Query("location") location: String): WeatherHourly

    /**
     * 空气质量
     */
    @GET("/v7/air/now?key=$HEFENG_KEY")
    suspend fun airWeather (@Query("location") location: String) : AirNow
}