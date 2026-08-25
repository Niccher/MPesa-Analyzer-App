package com.niccher.mpesa_analyzer_app.helpers

import java.util.regex.Pattern

data class MpesaTransaction(
    val amount: Float,
    val category: String
)

class MpesaParser {

    companion object {
        private val P2P_SENT_PATTERN = Pattern.compile("(?:Ksh|KES)\\s*([0-9,.]+)\\s+sent\\s+to|You\\s+have\\s+sent\\s+(?:Ksh|KES)\\s*([0-9,.]+)", Pattern.CASE_INSENSITIVE)
        private val PAYBILL_PATTERN = Pattern.compile("(?:Ksh|KES)\\s*([0-9,.]+)\\s+paid\\s+to|You\\s+have\\s+paid\\s+(?:Ksh|KES)\\s*([0-9,.]+)", Pattern.CASE_INSENSITIVE)
        private val BUY_GOODS_PATTERN = Pattern.compile("(?:Ksh|KES)\\s*([0-9,.]+)\\s+spent\\s+on\\s+Buy\\s+Goods|spent\\s+(?:Ksh|KES)\\s*([0-9,.]+)\\s+on\\s+Buy\\s+Goods", Pattern.CASE_INSENSITIVE)
        private val AIRTIME_PATTERN = Pattern.compile("(?:Ksh|KES)\\s*([0-9,.]+)\\s+of\\s+airtime\\s+purchased|bought\\s+(?:Ksh|KES)\\s*([0-9,.]+)\\s+of\\s+airtime", Pattern.CASE_INSENSITIVE)
        private val WITHDRAW_PATTERN = Pattern.compile("(?:Ksh|KES)\\s*([0-9,.]+)\\s+withdrawn\\s+from|Withdrawn\\s+(?:Ksh|KES)\\s*([0-9,.]+)\\s+from", Pattern.CASE_INSENSITIVE)
        private val P2P_RECEIVED_PATTERN = Pattern.compile("received\\s+(?:Ksh|KES)\\s*([0-9,.]+)|You\\s+have\\s+received\\s+(?:Ksh|KES)\\s*([0-9,.]+)", Pattern.CASE_INSENSITIVE)
        private val FULIZA_PATTERN = Pattern.compile("(?:Ksh|KES)\\s*([0-9,.]+)\\s+(?:used\\s+to\\s+settle\\s+your\\s+Fuliza|Fuliza\\s+M-Pesa)", Pattern.CASE_INSENSITIVE)
        private val M_SHWARI_PATTERN = Pattern.compile("(?:Ksh|KES)\\s*([0-9,.]+)\\s+(?:transferred\\s+to\\s+M-Shwari|from\\s+M-Shwari)", Pattern.CASE_INSENSITIVE)
        private val BANK_PATTERN = Pattern.compile("(?:Ksh|KES)\\s*([0-9,.]+)\\s+sent\\s+to\\s+(?:[A-Za-z\\s]+)\\s+bank", Pattern.CASE_INSENSITIVE)
        private val FEES_PATTERN = Pattern.compile("Transaction\\s+cost,\\s+(?:Ksh|KES)\\s*([0-9,.]+)", Pattern.CASE_INSENSITIVE)

        fun parseMessage(message: String): MpesaTransaction? {
            // Remove commas from amount strings for parsing
            fun cleanAmount(amt: String): Float = amt.replace(",", "").toFloatOrNull() ?: 0f

            val p2pMatch = P2P_SENT_PATTERN.matcher(message)
            if (p2pMatch.find()) {
                val amt = p2pMatch.group(1) ?: p2pMatch.group(2)
                if (amt != null) return MpesaTransaction(cleanAmount(amt), "Money Sent")
            }

            val paybillMatch = PAYBILL_PATTERN.matcher(message)
            if (paybillMatch.find()) {
                val amt = paybillMatch.group(1) ?: paybillMatch.group(2)
                if (amt != null) return MpesaTransaction(cleanAmount(amt), "PayBill")
            }

            val buyGoodsMatch = BUY_GOODS_PATTERN.matcher(message)
            if (buyGoodsMatch.find()) {
                val amt = buyGoodsMatch.group(1) ?: buyGoodsMatch.group(2)
                if (amt != null) return MpesaTransaction(cleanAmount(amt), "Buy Goods")
            }

            val airtimeMatch = AIRTIME_PATTERN.matcher(message)
            if (airtimeMatch.find()) {
                val amt = airtimeMatch.group(1) ?: airtimeMatch.group(2)
                if (amt != null) return MpesaTransaction(cleanAmount(amt), "Airtime")
            }

            val withdrawMatch = WITHDRAW_PATTERN.matcher(message)
            if (withdrawMatch.find()) {
                val amt = withdrawMatch.group(1) ?: withdrawMatch.group(2)
                if (amt != null) return MpesaTransaction(cleanAmount(amt), "Withdrawal")
            }

            val receivedMatch = P2P_RECEIVED_PATTERN.matcher(message)
            if (receivedMatch.find()) {
                val amt = receivedMatch.group(1) ?: receivedMatch.group(2)
                if (amt != null) return MpesaTransaction(cleanAmount(amt), "Money Received")
            }

            val fulizaMatch = FULIZA_PATTERN.matcher(message)
            if (fulizaMatch.find()) return MpesaTransaction(cleanAmount(fulizaMatch.group(1)!!), "Fuliza")

            val mshwariMatch = M_SHWARI_PATTERN.matcher(message)
            if (mshwariMatch.find()) return MpesaTransaction(cleanAmount(mshwariMatch.group(1)!!), "M-Shwari")

            val bankMatch = BANK_PATTERN.matcher(message)
            if (bankMatch.find()) return MpesaTransaction(cleanAmount(bankMatch.group(1)!!), "Bank Transfer")

            val feesMatch = FEES_PATTERN.matcher(message)
            if (feesMatch.find()) return MpesaTransaction(cleanAmount(feesMatch.group(1)!!), "Fees")

            return null
        }

        fun getSpendingByCategory(messages: List<String>): Map<String, Float> {
            val summary = mutableMapOf<String, Float>()
            for (msg in messages) {
                val tx = parseMessage(msg)
                if (tx != null) {
                    summary[tx.category] = summary.getOrDefault(tx.category, 0f) + tx.amount
                }
            }
            return summary
        }
    }
}
