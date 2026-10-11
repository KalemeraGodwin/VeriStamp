package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CorporateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CorporateEntityDao {
    @Query("SELECT * FROM corporate_entities ORDER BY createdAtMillis ASC")
    fun getAllEntitiesFlow(): Flow<List<CorporateEntity>>

    @Query("SELECT * FROM corporate_entities")
    suspend fun getAllEntitiesList(): List<CorporateEntity>

    @Query("SELECT * FROM corporate_entities WHERE id = :id LIMIT 1")
    suspend fun getEntityById(id: Long): CorporateEntity?

    @Query("SELECT COUNT(*) FROM corporate_entities")
    suspend fun getEntityCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntity(entity: CorporateEntity): Long

    @Update
    suspend fun updateEntity(entity: CorporateEntity)

    @Delete
    suspend fun deleteEntity(entity: CorporateEntity)
}
