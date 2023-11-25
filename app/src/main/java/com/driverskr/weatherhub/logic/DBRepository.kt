package com.driverskr.weatherhub.logic

import com.driverskr.lib.BaseApplication.Companion.context
import com.driverskr.weatherhub.logic.db.AppDatabase
import com.driverskr.weatherhub.logic.db.dao.CacheDao
import com.driverskr.weatherhub.logic.db.dao.CityDao

/**
 * @Author: driverSkr
 * @Time: 2023/11/25 11:53
 * @Description: 数据库操作仓$
 */
const val TIME_HOUR = 60 * 60
const val TIME_DAY = TIME_HOUR * 24

class DBRepository {

    private val cacheDao: CacheDao = AppDatabase.getInstance(context).cacheDao()
    private val cityDao: CityDao = AppDatabase.getInstance(context).cityDao()

    companion object {
        @Volatile
        private var instance: DBRepository? = null

        @JvmStatic
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: DBRepository().also { instance = it }
        }
    }
}