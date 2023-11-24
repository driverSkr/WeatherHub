package com.driverskr.weatherhub.logic.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.driverskr.weatherhub.logic.db.entity.CityEntity

/**
 * @Author: driverSkr
 * @Time: 2023/11/24 18:49
 * @Description: $
 */
@Dao
interface CityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addCity(city: CityEntity): Long

    //删除前定位城市
    @Query("update city set isLocal = 0 where cityId!=:cityId")
    fun removeLocal(cityId: String)

    //获取所有保存城市
    @Query("select * from city order by isLocal desc")
    fun getCities(): List<CityEntity>

    //删除指定id城市
    @Query("delete from city where cityId=:id")
    fun removeCity(id: String)

    //删除所有非定位城市
    @Query("delete from city where isLocal = 0")
    fun removeNotLocalCity()

    //删除所有城市
    @Query("delete from city")
    fun removeAllCity()
}