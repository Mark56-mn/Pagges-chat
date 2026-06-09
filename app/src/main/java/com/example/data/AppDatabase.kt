package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [
    WalletEntity::class, 
    TransactionEntity::class,
    ConversationEntity::class,
    MessageEntity::class,
    UserProfileEntity::class
], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}
