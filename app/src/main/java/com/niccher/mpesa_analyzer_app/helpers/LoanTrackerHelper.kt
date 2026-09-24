package com.niccher.mpesa_analyzer_app.helpers

import com.niccher.mpesa_analyzer_app.database.TransactionEntity

data class LoanSummary(
    val totalBorrowed: Float,
    val totalRepaid: Float,
    val totalFeesPaid: Float,
    val currentOutstanding: Float,
    val fulizaBorrowCount: Int,
    val mshwariBorrowCount: Int
)

object LoanTrackerHelper {

    fun calculateLoanSummary(transactions: List<TransactionEntity>): LoanSummary {
        var borrowed = 0f
        var repaid = 0f
        var fees = 0f
        var latestOutstanding = 0f
        var fulizaCount = 0
        var mshwariCount = 0

        // Transactions are ordered by timestamp DESC
        for (tx in transactions) {
            val parsed = MpesaParser.parseMessage(tx.body) ?: continue

            // Record latest reported outstanding overdraft
            if (parsed.fulizaOutstanding > 0f && latestOutstanding == 0f) {
                latestOutstanding = parsed.fulizaOutstanding
            }

            val bodyLower = tx.body.lowercase()
            if (parsed.category == "Fuliza" || bodyLower.contains("fuliza")) {
                if (bodyLower.contains("settle") || parsed.direction == "outgoing") {
                    repaid += parsed.amount
                } else {
                    val amt = if (parsed.fulizaAmount > 0f) parsed.fulizaAmount else parsed.amount
                    borrowed += amt
                    fulizaCount++
                }
                fees += parsed.fee
            } else if (parsed.category == "M-Shwari" || bodyLower.contains("m-shwari")) {
                if (bodyLower.contains("loan")) {
                    if (parsed.direction == "incoming" || bodyLower.contains("from m-shwari")) {
                        borrowed += parsed.amount
                        mshwariCount++
                    } else {
                        repaid += parsed.amount
                    }
                    fees += parsed.fee
                }
            }
        }

        return LoanSummary(
            totalBorrowed = borrowed,
            totalRepaid = repaid,
            totalFeesPaid = fees,
            currentOutstanding = latestOutstanding,
            fulizaBorrowCount = fulizaCount,
            mshwariBorrowCount = mshwariCount
        )
    }
}
