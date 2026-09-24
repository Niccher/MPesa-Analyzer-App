package com.niccher.mpesa_analyzer_app.helpers

import java.util.regex.Pattern

data class MpesaTransaction(
    val amount: Float,
    val category: String,
    val counterparty: String = "",
    val newBalance: Float? = null,
    val transactionCode: String = "",
    val fee: Float = 0f,
    val direction: String = "outgoing",
    val fulizaAmount: Float = 0f,
    val fulizaOutstanding: Float = 0f
)

class MpesaParser {

    companion object {
        private val CODE_PATTERN = Pattern.compile("^([A-Z0-9]{10})\\s+(?:Confirmed|is\\s+Confirmed)", Pattern.CASE_INSENSITIVE)
        private val BALANCE_PATTERN = Pattern.compile("(?:New\\s+)?(?:M-PESA|account)\\s+balance\\s+is\\s+(?:Ksh|KES)\\s*([0-9,.]+)", Pattern.CASE_INSENSITIVE)
        private val FEES_PATTERN = Pattern.compile("Transaction\\s+cost,\\s+(?:Ksh|KES)\\s*([0-9,.]+)", Pattern.CASE_INSENSITIVE)
        private val FULIZA_AMT_PATTERN = Pattern.compile("Fuliza\\s+M-PESA\\s+amount\\s+is\\s+(?:Ksh|KES)\\s*([0-9,.]+)", Pattern.CASE_INSENSITIVE)
        private val FULIZA_OUTSTANDING_PATTERN = Pattern.compile("(?:Total\\s+Fuliza\\s+outstanding|Outstanding\\s+balance)\\s+is\\s+(?:Ksh|KES)\\s*([0-9,.]+)", Pattern.CASE_INSENSITIVE)

        private val P2P_SENT_PATTERN = Pattern.compile("(?:(?:Ksh|KES)\\s*([0-9,.]+)\\s+sent\\s+to|You\\s+have\\s+sent\\s+(?:Ksh|KES)\\s*([0-9,.]+)\\s+to)\\s+([A-Za-z0-9\\s.'-]+?)(?:\\s+[0-9]{10,12})?\\s+on\\s+", Pattern.CASE_INSENSITIVE)
        private val PAYBILL_PATTERN = Pattern.compile("(?:(?:Ksh|KES)\\s*([0-9,.]+)\\s+paid\\s+to|You\\s+have\\s+paid\\s+(?:Ksh|KES)\\s*([0-9,.]+)\\s+to)\\s+([A-Za-z0-9\\s.'&-]+?)(?:\\s+for\\s+account|\\s+on\\s+|\\.)", Pattern.CASE_INSENSITIVE)
        private val BUY_GOODS_PATTERN = Pattern.compile("(?:(?:Ksh|KES)\\s*([0-9,.]+)\\s+spent\\s+on\\s+Buy\\s+Goods|spent\\s+(?:Ksh|KES)\\s*([0-9,.]+)\\s+on\\s+Buy\\s+Goods)(?:\\s+at\\s+|\\s+to\\s+|\\s+)([A-Za-z0-9\\s.'&-]+?)(?:\\s+on\\s+|\\.)", Pattern.CASE_INSENSITIVE)
        private val AIRTIME_PATTERN = Pattern.compile("(?:(?:Ksh|KES)\\s*([0-9,.]+)\\s+of\\s+airtime\\s+purchased|bought\\s+(?:Ksh|KES)\\s*([0-9,.]+)\\s+of\\s+airtime)", Pattern.CASE_INSENSITIVE)
        private val WITHDRAW_PATTERN = Pattern.compile("(?:(?:Ksh|KES)\\s*([0-9,.]+)\\s+withdrawn\\s+from|Withdrawn\\s+(?:Ksh|KES)\\s*([0-9,.]+)\\s+from|Withdraw\\s+(?:Ksh|KES)\\s*([0-9,.]+)\\s+from)\\s+([^.]+?)(?:\\s+on\\s+|\\.)", Pattern.CASE_INSENSITIVE)
        private val P2P_RECEIVED_PATTERN = Pattern.compile("(?:received\\s+(?:Ksh|KES)\\s*([0-9,.]+)|You\\s+have\\s+received\\s+(?:Ksh|KES)\\s*([0-9,.]+))(?:\\s+from\\s+([A-Za-z0-9\\s.'-]+?)(?:\\s+[0-9]{10,12})?\\s+on\\s+|)", Pattern.CASE_INSENSITIVE)
        private val FULIZA_PATTERN = Pattern.compile("(?:Ksh|KES)\\s*([0-9,.]+)\\s+(?:used\\s+to\\s+settle\\s+your\\s+Fuliza|Fuliza\\s+M-Pesa)", Pattern.CASE_INSENSITIVE)
        private val M_SHWARI_PATTERN = Pattern.compile("(?:Ksh|KES)\\s*([0-9,.]+)\\s+(?:transferred\\s+to\\s+M-Shwari|from\\s+M-Shwari)", Pattern.CASE_INSENSITIVE)
        private val BANK_PATTERN = Pattern.compile("(?:Ksh|KES)\\s*([0-9,.]+)\\s+sent\\s+to\\s+([A-Za-z\\s]+)\\s+bank", Pattern.CASE_INSENSITIVE)

        private fun cleanAmount(amt: String): Float =
            amt.replace(",", "").trimEnd('.').toFloatOrNull() ?: 0f

        fun parseMessage(message: String): MpesaTransaction? {
            // Extract common metadata
            var code = ""
            val codeM = CODE_PATTERN.matcher(message)
            if (codeM.find()) code = codeM.group(1).orEmpty()

            var balance: Float? = null
            val balM = BALANCE_PATTERN.matcher(message)
            if (balM.find()) {
                val bStr = balM.group(1)
                if (bStr != null) balance = cleanAmount(bStr)
            }

            var fee = 0f
            val feeM = FEES_PATTERN.matcher(message)
            if (feeM.find()) {
                val fStr = feeM.group(1)
                if (fStr != null) fee = cleanAmount(fStr)
            }

            var fulizaAmt = 0f
            val fulizaAmtM = FULIZA_AMT_PATTERN.matcher(message)
            if (fulizaAmtM.find()) {
                val faStr = fulizaAmtM.group(1)
                if (faStr != null) fulizaAmt = cleanAmount(faStr)
            }

            var fulizaOut = 0f
            val fulizaOutM = FULIZA_OUTSTANDING_PATTERN.matcher(message)
            if (fulizaOutM.find()) {
                val foStr = fulizaOutM.group(1)
                if (foStr != null) fulizaOut = cleanAmount(foStr)
            }

            // 1. P2P Sent
            val p2pMatch = P2P_SENT_PATTERN.matcher(message)
            if (p2pMatch.find()) {
                val amt = p2pMatch.group(1) ?: p2pMatch.group(2)
                val cp = (p2pMatch.group(3) ?: "").trim()
                if (amt != null) return MpesaTransaction(
                    amount = cleanAmount(amt),
                    category = "Money Sent",
                    counterparty = cp,
                    newBalance = balance,
                    transactionCode = code,
                    fee = fee,
                    direction = "outgoing",
                    fulizaAmount = fulizaAmt,
                    fulizaOutstanding = fulizaOut
                )
            }

            // 2. PayBill
            val paybillMatch = PAYBILL_PATTERN.matcher(message)
            if (paybillMatch.find()) {
                val amt = paybillMatch.group(1) ?: paybillMatch.group(2)
                val cp = (paybillMatch.group(3) ?: "").trim()
                if (amt != null) return MpesaTransaction(
                    amount = cleanAmount(amt),
                    category = "PayBill",
                    counterparty = cp,
                    newBalance = balance,
                    transactionCode = code,
                    fee = fee,
                    direction = "outgoing",
                    fulizaAmount = fulizaAmt,
                    fulizaOutstanding = fulizaOut
                )
            }

            // 3. Buy Goods
            val buyGoodsMatch = BUY_GOODS_PATTERN.matcher(message)
            if (buyGoodsMatch.find()) {
                val amt = buyGoodsMatch.group(1) ?: buyGoodsMatch.group(2)
                val cp = (buyGoodsMatch.group(3) ?: "").trim()
                if (amt != null) return MpesaTransaction(
                    amount = cleanAmount(amt),
                    category = "Buy Goods",
                    counterparty = cp,
                    newBalance = balance,
                    transactionCode = code,
                    fee = fee,
                    direction = "outgoing",
                    fulizaAmount = fulizaAmt,
                    fulizaOutstanding = fulizaOut
                )
            }

            // 4. Airtime
            val airtimeMatch = AIRTIME_PATTERN.matcher(message)
            if (airtimeMatch.find()) {
                val amt = airtimeMatch.group(1) ?: airtimeMatch.group(2)
                if (amt != null) return MpesaTransaction(
                    amount = cleanAmount(amt),
                    category = "Airtime",
                    counterparty = "Safaricom Airtime",
                    newBalance = balance,
                    transactionCode = code,
                    fee = fee,
                    direction = "outgoing",
                    fulizaAmount = fulizaAmt,
                    fulizaOutstanding = fulizaOut
                )
            }

            // 5. Withdrawal
            val withdrawMatch = WITHDRAW_PATTERN.matcher(message)
            if (withdrawMatch.find()) {
                val amt = withdrawMatch.group(1) ?: withdrawMatch.group(2) ?: withdrawMatch.group(3)
                val cp = (withdrawMatch.group(4) ?: "").trim()
                if (amt != null) return MpesaTransaction(
                    amount = cleanAmount(amt),
                    category = "Withdrawal",
                    counterparty = cp,
                    newBalance = balance,
                    transactionCode = code,
                    fee = fee,
                    direction = "outgoing",
                    fulizaAmount = fulizaAmt,
                    fulizaOutstanding = fulizaOut
                )
            }

            // 6. Received
            val receivedMatch = P2P_RECEIVED_PATTERN.matcher(message)
            if (receivedMatch.find()) {
                val amt = receivedMatch.group(1) ?: receivedMatch.group(2)
                val cp = (receivedMatch.group(3) ?: "").trim()
                if (amt != null) return MpesaTransaction(
                    amount = cleanAmount(amt),
                    category = "Money Received",
                    counterparty = cp,
                    newBalance = balance,
                    transactionCode = code,
                    fee = fee,
                    direction = "incoming",
                    fulizaAmount = fulizaAmt,
                    fulizaOutstanding = fulizaOut
                )
            }

            // 7. Fuliza
            val fulizaMatch = FULIZA_PATTERN.matcher(message)
            if (fulizaMatch.find()) {
                val amt = fulizaMatch.group(1)!!
                val isRepayment = message.contains("settle", ignoreCase = true)
                return MpesaTransaction(
                    amount = cleanAmount(amt),
                    category = "Fuliza",
                    counterparty = if (isRepayment) "Fuliza Repayment" else "Fuliza Overdraft",
                    newBalance = balance,
                    transactionCode = code,
                    fee = fee,
                    direction = if (isRepayment) "outgoing" else "incoming",
                    fulizaAmount = fulizaAmt,
                    fulizaOutstanding = fulizaOut
                )
            }

            // 8. M-Shwari
            val mshwariMatch = M_SHWARI_PATTERN.matcher(message)
            if (mshwariMatch.find()) {
                val amt = mshwariMatch.group(1)!!
                val isDeposit = message.contains("transferred to", ignoreCase = true)
                return MpesaTransaction(
                    amount = cleanAmount(amt),
                    category = "M-Shwari",
                    counterparty = if (isDeposit) "M-Shwari Savings" else "M-Shwari Loan",
                    newBalance = balance,
                    transactionCode = code,
                    fee = fee,
                    direction = if (isDeposit) "outgoing" else "incoming",
                    fulizaAmount = fulizaAmt,
                    fulizaOutstanding = fulizaOut
                )
            }

            // 9. Bank Transfer
            val bankMatch = BANK_PATTERN.matcher(message)
            if (bankMatch.find()) {
                val amt = bankMatch.group(1)!!
                val cp = (bankMatch.group(2) ?: "Bank").trim() + " Bank"
                return MpesaTransaction(
                    amount = cleanAmount(amt),
                    category = "Bank Transfer",
                    counterparty = cp,
                    newBalance = balance,
                    transactionCode = code,
                    fee = fee,
                    direction = "outgoing",
                    fulizaAmount = fulizaAmt,
                    fulizaOutstanding = fulizaOut
                )
            }

            // 10. Fees Only
            if (feeM.find()) {
                val fAmt = feeM.group(1)!!
                return MpesaTransaction(
                    amount = cleanAmount(fAmt),
                    category = "Fees",
                    counterparty = "Safaricom Fee",
                    newBalance = balance,
                    transactionCode = code,
                    fee = cleanAmount(fAmt),
                    direction = "outgoing",
                    fulizaAmount = fulizaAmt,
                    fulizaOutstanding = fulizaOut
                )
            }

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
