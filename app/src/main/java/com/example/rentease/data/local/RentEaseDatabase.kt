package com.example.rentease.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rentease.data.model.Landlord
import com.example.rentease.data.model.Property
import com.example.rentease.data.model.UserRequest

@Database(
    entities = [Landlord::class, Property::class, UserRequest::class],
    version = 1,
    exportSchema = false
)
abstract class RentEaseDatabase : RoomDatabase() {
    abstract fun landlordDao(): LandlordDao
    abstract fun propertyDao(): PropertyDao
    abstract fun userRequestDao(): UserRequestDao

    companion object {
        @Volatile
        private var INSTANCE: RentEaseDatabase? = null

        fun getDatabase(context: Context): RentEaseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RentEaseDatabase::class.java,
                    "rentease_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
