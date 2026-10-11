package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TicketEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets WHERE eventId = :eventId ORDER BY id DESC")
    fun getTicketsForEventFlow(eventId: Long): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets ORDER BY id DESC")
    fun getAllTicketsFlow(): Flow<List<TicketEntity>>

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: Long): TicketEntity?

    @Query("SELECT * FROM tickets WHERE id = :id LIMIT 1")
    fun getTicketByIdFlow(id: Long): Flow<TicketEntity?>

    @Query("SELECT * FROM tickets WHERE serialNumber = :serialNumber LIMIT 1")
    suspend fun getTicketBySerialNumber(serialNumber: String): TicketEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTicket(ticket: TicketEntity): Long

    @Update
    suspend fun updateTicket(ticket: TicketEntity)

    @Query("UPDATE tickets SET checkedIn = 1, checkedInAt = :checkedInAt, checkedInAtMillis = :timestampMillis WHERE id = :ticketId")
    suspend fun markTicketCheckedIn(ticketId: Long, checkedInAt: String, timestampMillis: Long)

    @Delete
    suspend fun deleteTicket(ticket: TicketEntity)

    @Query("SELECT COUNT(*) FROM tickets WHERE eventId = :eventId")
    fun getTicketCountFlow(eventId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM tickets WHERE eventId = :eventId AND checkedIn = 1")
    fun getCheckedInCountFlow(eventId: Long): Flow<Int>
}
