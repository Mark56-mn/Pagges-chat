package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PendingSyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: PendingSyncEntity)

    @Query("SELECT * FROM pending_sync ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<PendingSyncEntity>

    @Query("DELETE FROM pending_sync WHERE id = :id")
    suspend fun deleteOperation(id: String)
}
