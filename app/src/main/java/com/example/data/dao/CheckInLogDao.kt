package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.CheckInLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInLogDao {
    @Query("SELECT * FROM check_in_logs ORDER BY timestampMillis DESC")
    fun getAllLogsFlow(): Flow<List<CheckInLogEntity>>

    @Query("SELECT * FROM check_in_logs WHERE eventName = :eventName ORDER BY timestampMillis DESC")
    fun getLogsByEventNameFlow(eventName: String): Flow<List<CheckInLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: CheckInLogEntity): Long

    @Query("DELETE FROM check_in_logs")
    suspend fun clearAllLogs()
}
