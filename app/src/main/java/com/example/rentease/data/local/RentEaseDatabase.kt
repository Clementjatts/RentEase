package com.example.rentease.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.rentease.data.model.FavoriteProperty
import com.example.rentease.data.model.Landlord
import com.example.rentease.data.model.Property
import com.example.rentease.data.model.UserRequest
import com.example.rentease.data.model.UserEntity

@Database(
    entities = [
        Landlord::class,
        Property::class, 
        UserRequest::class,
        FavoriteProperty::class,
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class RentEaseDatabase : RoomDatabase() {
    abstract fun landlordDao(): LandlordDao
    abstract fun propertyDao(): PropertyDao
    abstract fun userRequestDao(): UserRequestDao
    abstract fun favoritePropertyDao(): FavoritePropertyDao
    abstract fun userDao(): UserDao

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
                .fallbackToDestructiveMigration() // This will destroy data if migration fails
                .addMigrations(DatabaseMigrations.MIGRATION_1_2) // Add migration path
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
