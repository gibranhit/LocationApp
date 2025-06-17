package com.gibran.locationapp.data.local.dao

import androidx.room.*
import com.gibran.locationapp.data.local.entities.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity)

    @Query("DELETE FROM cities WHERE id = :id")
    suspend fun deleteCityById(id: String)

    @Query("SELECT id FROM cities WHERE isFavorite = 1")
    suspend fun getFavoriteIds(): List<String>
    
    @Query("SELECT * FROM cities WHERE isFavorite = 1")
    fun getFavoriteCitiesFlow(): Flow<List<CityEntity>>
    
    @Transaction
    suspend fun toggleFavorite(cityId: String): Boolean {
        val favoriteIds = getFavoriteIds()
        val isFavorite = favoriteIds.contains(cityId)
        
        if (isFavorite) {
            // Remove from favorites - delete the entity
            deleteCityById(cityId)
            return false
        } else {
            // Add to favorites - insert minimal entity
            val favoriteEntity = CityEntity(
                id = cityId,
                name = "", // We'll get the name from JSON
                country = "",
                latitude = 0.0,
                longitude = 0.0,
                isFavorite = true
            )
            insertCity(favoriteEntity)
            return true
        }
    }
}
