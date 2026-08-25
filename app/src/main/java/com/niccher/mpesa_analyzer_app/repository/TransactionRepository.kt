package com.niccher.mpesa_analyzer_app.repository

import com.niccher.mpesa_analyzer_app.database.TransactionDao
import com.niccher.mpesa_analyzer_app.database.TransactionEntity
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    suspend fun insert(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun insertAll(transactions: List<TransactionEntity>) {
        transactionDao.insertTransactions(transactions)
    }

    fun getTransactionsByCategory(category: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByCategory(category)
    }

    suspend fun getMaxSmsId(): Long {
        return transactionDao.getMaxSmsId() ?: -1L
    }

    suspend fun getUnsynced(): List<TransactionEntity> {
        return transactionDao.getUnsyncedTransactions()
    }

    suspend fun markSynced(localIds: List<Long>) {
        transactionDao.markAsSynced(localIds)
    }
}
