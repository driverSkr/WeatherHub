package com.driverskr.weatherhub.logic

import com.driverskr.lib.BaseApplication.Companion.context
import com.driverskr.weatherhub.logic.db.AppDatabase
import com.driverskr.weatherhub.logic.db.dao.CacheDao
import com.driverskr.weatherhub.logic.db.dao.CityDao
import com.driverskr.weatherhub.logic.db.entity.CacheEntity
import com.driverskr.weatherhub.logic.db.entity.CityEntity
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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

    fun removeLocal(cityId: String) {
        cityDao.removeLocal(cityId)
    }

    suspend fun addCity(city: CityEntity) {
        cityDao.addCity(city)
    }

    suspend fun removeCity(cityId: String) {
        cityDao.removeCity(cityId)
    }

    suspend fun removeNotLocalCity() {
        cityDao.removeNotLocalCity()
    }

    suspend fun removeAllCity() {
        cityDao.removeAllCity()
    }

    suspend fun getCities(): List<CityEntity> {
        return cityDao.getCities()
    }

    suspend fun deleteCache(key: String) {
        val cache = CacheEntity()
        cache.key = key
        cacheDao.deleteCache(cache)
    }

    suspend fun <T> saveCache(key: String, body: T) {
        saveCache(key, body, 0)
    }

    suspend fun <T> saveCache(key: String, body: T, saveTime: Int) {
        val cache = CacheEntity()
        cache.key = key
        cache.data = toByteArray(body)
        if (saveTime == 0) {
            cache.dead_line = 0
        } else {
            cache.dead_line = System.currentTimeMillis() / 1000 + saveTime
        }
        cacheDao.saveCache(cache)
    }

    suspend fun <T> getCache(key: String): T? {
        val cache: CacheEntity? = cacheDao.getCache(key)
        return if (cache?.data != null) {
            if (cache.dead_line == 0L) {
                toObject(cache.data) as T
            } else {
                if (cache.dead_line > System.currentTimeMillis() / 1000) {
                    toObject(cache.data) as T
                } else {
                    null
                }
            }
        } else {
            null
        }
    }

    //序列化存储数据需要转换成二进制
    private fun <T> toByteArray(body: T): ByteArray {
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(body)
        oos.flush()
        oos.close()
        return baos.toByteArray()
    }

    //反序列,把二进制数据转换成java object对象
    private fun toObject(data: ByteArray?): Any? {
        val bais = ByteArrayInputStream(data)
        val ois = ObjectInputStream(bais)
        val readObject = ois.readObject()
        ois.close()
        return readObject
    }

    companion object {
        @Volatile
        private var instance: DBRepository? = null

        @JvmStatic
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: DBRepository().also { instance = it }
        }
    }
}