package com.gibran.locationapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.gibran.locationapp.data.local.dao.CityDao
import com.gibran.locationapp.data.local.entities.CityEntity

@Database(
    entities = [CityEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun cityDao(): CityDao

    companion object {
        const val DATABASE_NAME = "location_app_database"
    }
}
