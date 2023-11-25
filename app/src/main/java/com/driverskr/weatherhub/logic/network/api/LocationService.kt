package com.driverskr.weatherhub.logic.network.api

import com.driverskr.weatherhub.bean.SearchCity
import com.driverskr.weatherhub.utils.Constant.HEFENG_KEY
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 11:34
 * @Description: 城市api$
 */
interface LocationService {

    @GET("/v2/city/lookup?key=$HEFENG_KEY&range=cn")
    suspend fun searchCity(@Query("location") location: String,
                           @Query("mode") mode: String): SearchCity
}