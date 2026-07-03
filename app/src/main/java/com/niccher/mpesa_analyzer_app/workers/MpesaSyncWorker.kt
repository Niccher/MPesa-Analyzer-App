package com.niccher.mpesa_analyzer_app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class MpesaSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Midnight Cutoff Logic (00:00 to 19:59)
        // If it's before 8:00 PM (20:00), we return failure to stop retrying until the next scheduled 8PM trigger.
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        if (currentHour < 20) {
            return@withContext Result.failure()
        }

        try {
            // TODO: Fetch locally parsed M-PESA transactions from DB
            // val transactions = db.getUnsyncedTransactions()
            
            // TODO: Execute Retrofit upload API call
            // val response = apiService.upload(transactions)
            
            // if (response.isSuccessful) {
            //     db.markAsSynced(transactions)
            //     return@withContext Result.success()
            // } else {
            //     return@withContext Result.retry() 
            // }
            
            // Simulating successful upload for now
            return@withContext Result.success()

        } catch (e: Exception) {
            e.printStackTrace()
            // On network failure or exception, retry it in 30 minutes (BackoffPolicy matches)
            return@withContext Result.retry()
        }
    }
}
