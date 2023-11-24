package com.driverskr.weatherhub.logic.db.entity

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * @Author: driverSkr
 * @Time: 2023/11/24 18:30
 * @Description: $
 */
@Entity(tableName = "cache")
class CacheEntity {

    @PrimaryKey(autoGenerate = false)
    @NonNull
    var key: String = ""

    //缓存数据的二进制
    var data: ByteArray? = null

    var dead_line: Long = 0
}