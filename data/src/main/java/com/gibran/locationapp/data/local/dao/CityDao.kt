package com.gibran.locationapp.data.local.dao

import androidx.room.*
import com.gibran.locationapp.data.local.entities.CityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CityDao {
    
    @Query("SELECT * FROM cities ORDER BY name ASC")
    fun getAllCities(): Flow<List<CityEntity>>
    
    @Query("SELECT * FROM cities WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteCities(): Flow<List<CityEntity>>
    
    @Query("SELECT * FROM cities WHERE name LIKE :query OR country LIKE :query ORDER BY name ASC")
    fun searchCities(query: String): Flow<List<CityEntity>>
    
    @Query("SELECT * FROM cities WHERE id = :id LIMIT 1")
    suspend fun getCityById(id: String): CityEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCities(cities: List<CityEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity)
    
    @Update
    suspend fun updateCity(city: CityEntity)
    
    @Query("UPDATE cities SET isFavorite = :isFavorite WHERE id = :cityId")
    suspend fun updateFavoriteStatus(cityId: String, isFavorite: Boolean)
    
    @Query("DELETE FROM cities")
    suspend fun deleteAllCities()
    
    @Query("DELETE FROM cities WHERE id = :id")
    suspend fun deleteCityById(id: String)
    
    @Query("SELECT * FROM cities WHERE distance IS NOT NULL ORDER BY distance ASC")
    fun getCitiesSortedByDistance(): Flow<List<CityEntity>>
    
    @Query("UPDATE cities SET distance = :distance WHERE id = :cityId")
    suspend fun updateCityDistance(cityId: String, distance: Double)
    
    @Query("SELECT COUNT(*) FROM cities")
    suspend fun getCitiesCount(): Int
    
    // New methods for hybrid architecture
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
