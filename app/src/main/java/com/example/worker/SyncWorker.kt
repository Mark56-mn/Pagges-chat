package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.local.PendingSyncEntity

class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val pendingSyncDao = database.pendingSyncDao()
        
        val pendingOperations = pendingSyncDao.getPendingOperations()

        if (pendingOperations.isEmpty()) {
            return Result.success()
        }

        var hasFailures = false

        for (operation in pendingOperations) {
            try {
                // Here we would sync with Supabase based on operation.tableName & operation.operationType
                // For demonstration, simulating network sync
                // if (operation.tableName == "messages") { ...SupabaseManager.insert(...) }
                
                Log.d("SyncWorker", "Synced operation: ${operation.id}")
                pendingSyncDao.deleteOperation(operation.id)
                // Mark record in DAO as synced = true as needed
            } catch (e: Exception) {
                Log.e("SyncWorker", "Failed to sync operation: ${operation.id}", e)
                if (operation.retryCount < 3) {
                    val updatedOperation = operation.copy(retryCount = operation.retryCount + 1)
                    pendingSyncDao.insert(updatedOperation)
                    hasFailures = true
                } else {
                    // Max retries reached, you could either mark as FAILED or drop it
                    pendingSyncDao.deleteOperation(operation.id)
                }
            }
        }

        return if (hasFailures) {
            Result.retry()
        } else {
            Result.success()
        }
    }
}
