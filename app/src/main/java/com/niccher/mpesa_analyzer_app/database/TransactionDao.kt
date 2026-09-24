package com.niccher.mpesa_analyzer_app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE syncStatus = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Query("UPDATE transactions SET syncStatus = 1 WHERE localId IN (:localIds)")
    suspend fun markAsSynced(localIds: List<Long>)

    @Query("SELECT * FROM transactions WHERE category = :category ORDER BY timestamp DESC")
    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>>

    @Query("SELECT MAX(smsId) FROM transactions")
    suspend fun getMaxSmsId(): Long?

    @Query("SELECT SUM(amount) FROM transactions WHERE direction = 'outgoing' AND timestamp >= :startTime")
    fun getOutflowSince(startTime: Long): Flow<Float?>

    @Query("SELECT SUM(amount) FROM transactions WHERE direction = 'outgoing' AND timestamp >= :startOfDay")
    suspend fun getTodaySpend(startOfDay: Long): Float?

    @Query("SELECT * FROM transactions WHERE category IN ('Fuliza', 'M-Shwari') OR body LIKE '%Fuliza%' OR body LIKE '%M-Shwari%' ORDER BY timestamp DESC")
    fun getLoanTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT 1")
    fun getLatestTransaction(): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentTransactions(limit: Int): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
