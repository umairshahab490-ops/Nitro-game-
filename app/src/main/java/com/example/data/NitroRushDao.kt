package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NitroRushDao {
    @Query("SELECT * FROM run_records ORDER BY score DESC, timestampMillis DESC LIMIT 50")
    fun observeTopRuns(): Flow<List<RunRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(record: RunRecordEntity)

    @Query("SELECT * FROM cached_routes ORDER BY savedAtMillis DESC LIMIT 20")
    fun observeCachedRoutes(): Flow<List<CachedRouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoutes(routes: List<CachedRouteEntity>)

    @Query("DELETE FROM cached_routes WHERE isGpsDerived = 1")
    suspend fun clearGpsRoutes()
}
