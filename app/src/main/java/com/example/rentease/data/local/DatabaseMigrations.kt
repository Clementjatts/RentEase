package com.example.rentease.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations to handle schema changes between versions
 */
object DatabaseMigrations {
    
    /**
     * Migration from version 1 to version 2
     * - Adds users table
     */
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create the users table with column names that match Java/Kotlin fields
            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `users` (
                    `id` INTEGER NOT NULL,
                    `username` TEXT NOT NULL,
                    `email` TEXT,
                    `userType` TEXT NOT NULL, 
                    `fullName` TEXT,
                    `phone` TEXT,
                    `createdAt` TEXT NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """
            )
        }
    }
}
