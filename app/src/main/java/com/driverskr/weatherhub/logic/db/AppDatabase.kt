package com.driverskr.weatherhub.logic.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.driverskr.lib.extension.logE
import com.driverskr.weatherhub.logic.db.dao.CacheDao
import com.driverskr.weatherhub.logic.db.dao.CityDao
import com.driverskr.weatherhub.logic.db.entity.CacheEntity
import com.driverskr.weatherhub.logic.db.entity.CityEntity

/**
 * @Author: driverSkr
 * @Time: 2023/11/24 18:54
 * @Description: $
 */
@Database(entities = [CacheEntity::class, CityEntity::class], version = 1, exportSchema = false)
internal abstract class AppDatabase: RoomDatabase() {

    abstract fun cacheDao(): CacheDao

    abstract fun cityDao(): CityDao

    companion object {
        private const val DATABASE_NAME = "weather-hub.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also{ instance = it}
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .allowMainThreadQueries()
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        logE("driverSkr","db：onCreate")
                    }
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                    }
                }).build()
        }
    }
}