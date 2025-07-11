package com.gibran.locationapp.data.repositories

import android.content.Context
import android.util.Log
import com.gibran.locationapp.data.di.DefaultDispatcher
import com.gibran.locationapp.data.di.IoDispatcher
import com.gibran.locationapp.data.local.dao.CityDao
import com.gibran.locationapp.data.models.CityDto
import com.gibran.locationapp.data.models.toDomain
import com.gibran.locationapp.domain.models.City
import com.gibran.locationapp.domain.repositories.CitiesRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CitiesRepositoryImpl @Inject constructor(
    private val citiesFile: File,
    private val okHttpClient: OkHttpClient,
    private val cityDao: CityDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : CitiesRepository {

    companion object {
        private const val TAG = "CitiesRepository"
        const val CITIES_FILE_NAME = "cities.json"
        private const val CACHE_EXPIRY_DAYS = 7L
        private const val PREFIX_LENGTH = 3
        private const val CHUNK_SIZE = 1000
        private const val CITIES_URL = "https://gist.githubusercontent.com/hernan-uala/dce8843a8edbe0b0018b32e137bc2b3a/raw/0996accf70cb0ca0e16f9a99e0ee185fafca7af1/cities.json"
    }

    private val mutex = Mutex()
    private var cachedCities: List<City> = emptyList()
    private val prefixMap = mutableMapOf<String, MutableList<City>>()
    private var isIndexBuilt = false
    private var isDownloading = false

    private val cacheExpiryTime = CACHE_EXPIRY_DAYS * 24 * 60 * 60 * 1000L

    // Reactive flow observing favorite changes from Room
    private val favoritesFlow = cityDao.getFavoriteCitiesFlow()
        .map { entities -> entities.map { it.id }.toSet() }
        .distinctUntilChanged()

    override suspend fun getCities(): Flow<List<City>> =
        combine(getCitiesDataFlow(), favoritesFlow) { cities, favoriteIds ->
            cities.map { city -> city.copy(isFavorite = favoriteIds.contains(city.id)) }
        }.catch { error ->
            Log.e(TAG, "Error in getCities flow", error)
            emit(emptyList())
        }

    override suspend fun searchCities(query: String): Flow<List<City>> =
        combine(getSearchResultsFlow(query), favoritesFlow) { cities, favoriteIds ->
            cities.map { city -> city.copy(isFavorite = favoriteIds.contains(city.id)) }
        }.catch { error ->
            Log.e(TAG, "Error in searchCities flow", error)
            emit(emptyList())
        }


    override suspend fun toggleFavorite(cityId: String): Boolean = cityDao.toggleFavorite(cityId)

    override suspend fun getCityById(id: String): City? = mutex.withLock {
        val favoriteIds = runCatching { cityDao.getFavoriteIds() }.getOrElse { emptyList() }
        cachedCities.find { it.id == id }?.copy(isFavorite = favoriteIds.contains(id))
    }

    // Private methods for better organization

    private fun getCitiesDataFlow(): Flow<List<City>> = flow {
        val currentCities = mutex.withLock { cachedCities }

        if (currentCities.isNotEmpty()) {
            emit(currentCities)
        } else {
            loadCitiesData()?.let { emit(it) } ?: emit(emptyList())
        }
    }

    private fun getSearchResultsFlow(query: String): Flow<List<City>> = flow {
        val cities = mutex.withLock { cachedCities }

        when {
            cities.isEmpty() -> emit(emptyList())
            query.isEmpty() -> emit(cities)
            else -> emit(performSearch(query, cities))
        }
    }

    private suspend fun loadCitiesData(): List<City>? {
        val shouldDownload = mutex.withLock {
            if (cachedCities.isEmpty() && !isDownloading) {
                isDownloading = true
                true
            } else false
        }

        return if (shouldDownload) {
            try {
                Log.d(TAG, "Loading cities...")

                val cities = if (isCacheValid()) {
                    Log.d(TAG, "Loading from cache...")
                    loadCitiesFromFile()
                } else {
                    Log.d(TAG, "Downloading fresh data...")
                    downloadAndCacheCities()
                }

                mutex.withLock {
                    cachedCities = cities
                }

                withContext(defaultDispatcher) {
                    buildSearchIndex()
                }

                Log.d(TAG, "Loaded ${cities.size} cities successfully")
                cities
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load cities", e)
                null
            } finally {
                mutex.withLock { isDownloading = false }
            }
        } else null
    }

    private suspend fun performSearch(query: String, cities: List<City>): List<City> {
        ensureSearchIndexBuilt(cities)

        return withContext(defaultDispatcher) {
            val key = query.take(PREFIX_LENGTH).lowercase()
            prefixMap[key]
                ?.filter { it.name.startsWith(query, ignoreCase = true) }
                ?.sortedBy { it.name }
                ?: emptyList()
        }
    }

    private suspend fun ensureSearchIndexBuilt(cities: List<City>) {
        val indexBuilt = mutex.withLock { isIndexBuilt }
        if (!indexBuilt && cities.isNotEmpty()) {
            withContext(defaultDispatcher) {
                mutex.withLock {
                    if (!isIndexBuilt && cachedCities.isNotEmpty()) {
                        buildSearchIndex()
                    }
                }
            }
        }
    }

    private suspend fun downloadAndCacheCities(): List<City> = withContext(ioDispatcher) {
        Log.d(TAG, "Downloading cities JSON...")

        val request = Request.Builder()
            .url(CITIES_URL)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Failed to download cities: ${response.code}")
            }

            val jsonString = response.body?.string() ?: throw Exception("Empty response body")
            Log.d(TAG, "Downloaded ${jsonString.length} bytes")

            // Cache to file
            FileOutputStream(citiesFile).use { it.write(jsonString.toByteArray()) }
            Log.d(TAG, "Cached to: ${citiesFile.absolutePath}")

            parseCitiesFromJson(jsonString)
        }
    }

    private suspend fun loadCitiesFromFile(): List<City> = withContext(ioDispatcher) {
        Log.d(TAG, "Reading from cache...")
        val jsonString = FileInputStream(citiesFile).use {
            it.readBytes().toString(Charsets.UTF_8)
        }
        parseCitiesFromJson(jsonString)
    }

    private suspend fun parseCitiesFromJson(jsonString: String): List<City> = withContext(defaultDispatcher) {
        Log.d(TAG, "Parsing JSON...")

        val listType = Types.newParameterizedType(List::class.java, CityDto::class.java)
        val moshi = Moshi.Builder()
            .build()
        val adapter = moshi.adapter<List<CityDto>>(listType)

        val cityDtos = adapter.fromJson(jsonString) ?: emptyList()
        Log.d(TAG, "Parsed ${cityDtos.size} cities")

        cityDtos.toDomain()
    }

    private fun isCacheValid(): Boolean {
        if (!citiesFile.exists()) {
            Log.d(TAG, "Cache file doesn't exist")
            return false
        }

        val fileAge = System.currentTimeMillis() - citiesFile.lastModified()
        val isValid = fileAge < cacheExpiryTime
        
        Log.d(TAG, "Cache age: ${fileAge / (1000 * 60 * 60)} hours, valid: $isValid")
        return isValid
    }

    private fun buildSearchIndex() {
        Log.d(TAG, "Building search index for ${cachedCities.size} cities...")
        
        prefixMap.clear()
        
        // Process in chunks for better memory management
        cachedCities.chunked(CHUNK_SIZE).forEach { chunk ->
            chunk.forEach { city ->
                val key = city.name.take(PREFIX_LENGTH).lowercase()
                prefixMap.getOrPut(key) { mutableListOf() }.add(city)
            }
        }
        
        isIndexBuilt = true
        Log.d(TAG, "Search index built successfully")
    }
}
