package com.driverskr.weatherhub.logic.db.dao

import androidx.room.*
import com.driverskr.weatherhub.logic.db.entity.CacheEntity

/**
 * @Author: driverSkr
 * @Time: 2023/11/24 18:46
 * @Description: $
 */
@Dao
interface CacheDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveCache(cache: CacheEntity): Long

    @Query("select * from cache where `key`=:key")
    fun getCache(key: String): CacheEntity

    @Delete
    fun deleteCache(cache: CacheEntity): Int
}