package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CheckInLogDao
import com.example.data.dao.CorporateEntityDao
import com.example.data.dao.EntityProfileDao
import com.example.data.dao.EventDao
import com.example.data.dao.ReceiptDao
import com.example.data.dao.TicketDao
import com.example.data.model.CheckInLogEntity
import com.example.data.model.CorporateEntity
import com.example.data.model.EntityProfile
import com.example.data.model.EventEntity
import com.example.data.model.ReceiptEntity
import com.example.data.model.TicketEntity

@Database(
    entities = [
        EntityProfile::class,
        CorporateEntity::class,
        EventEntity::class,
        TicketEntity::class,
        ReceiptEntity::class,
        CheckInLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class VeriStampDatabase : RoomDatabase() {
    abstract fun entityProfileDao(): EntityProfileDao
    abstract fun corporateEntityDao(): CorporateEntityDao
    abstract fun eventDao(): EventDao
    abstract fun ticketDao(): TicketDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun checkInLogDao(): CheckInLogDao

    companion object {
        @Volatile
        private var INSTANCE: VeriStampDatabase? = null

        fun getInstance(context: Context): VeriStampDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VeriStampDatabase::class.java,
                    "veristamp_corporate.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
