package com.niccher.mpesa_analyzer_app.models

import com.google.gson.annotations.SerializedName

object FinancialAnalystModel {

    data class FinancialHealthResponse(
        @SerializedName("status") val status: Int,
        @SerializedName("score") val score: Int,
        @SerializedName("color") val color: String,
        @SerializedName("tips") val tips: List<String>
    )

    data class SmartAlert(
        @SerializedName("type") val type: String,
        @SerializedName("level") val level: String,
        @SerializedName("title") val title: String,
        @SerializedName("message") val message: String
    )

    data class SmartAlertsResponse(
        @SerializedName("status") val status: Int,
        @SerializedName("alerts") val alerts: List<SmartAlert>?
    )

    data class RecurringPayment(
        @SerializedName("counterparty") val counterparty: String,
        @SerializedName("amount") val amount: Double,
        @SerializedName("occurs") val occurs: Int,
        @SerializedName("last_paid") val last_paid: String?
    )

    data class RecurringPaymentsResponse(
        @SerializedName("status") val status: Int,
        @SerializedName("payments") val payments: List<RecurringPayment>?
    )

    data class SpendingTrends(
        @SerializedName("this_month") val this_month: Double,
        @SerializedName("last_month") val last_month: Double,
        @SerializedName("percentage") val percentage: Double,
        @SerializedName("trend") val trend: String
    )

    data class SpendingTrendsResponse(
        @SerializedName("status") val status: Int,
        @SerializedName("trends") val trends: SpendingTrends?
    )

    data class AIObservation(
        @SerializedName("type") val type: String,
        @SerializedName("label") val label: String,
        @SerializedName("icon") val icon: String,
        @SerializedName("text") val text: String
    )

    data class AIObservationsResponse(
        @SerializedName("status") val status: Int,
        @SerializedName("insights") val insights: List<AIObservation>?
    )
}
