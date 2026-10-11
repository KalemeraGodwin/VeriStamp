package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.EntityProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface EntityProfileDao {
    @Query("SELECT * FROM entity_profile WHERE id = 1 LIMIT 1")
    fun getProfileFlow(): Flow<EntityProfile?>

    @Query("SELECT * FROM entity_profile WHERE id = 1 LIMIT 1")
    suspend fun getProfile(): EntityProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: EntityProfile)

    @Update
    suspend fun update(profile: EntityProfile)

    @Query("UPDATE entity_profile SET isDarkMode = :isDark WHERE id = 1")
    suspend fun updateThemeMode(isDark: Boolean)
}
