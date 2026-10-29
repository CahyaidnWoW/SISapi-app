package com.latihan.si_sapimobile.models

import com.google.gson.annotations.SerializedName

data class DashboardSummaryResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: DashboardSummaryData
)

data class DashboardSummaryData(
    @SerializedName("total_cattle") val totalCattle: Int = 0,
    @SerializedName("ready_to_sell") val readyToSell: Int = 0,
    @SerializedName("sick_cattle") val sickCattle: Int = 0,
    @SerializedName("critical_feed_count") val criticalFeedCount: Int = 0,
    @SerializedName("avg_adg_30d") val avgAdg30d: Double = 0.0,
    @SerializedName("upcoming_medicals") val upcomingMedicals: List<UpcomingMedicalItem> = emptyList()
)

data class UpcomingMedicalItem(
    @SerializedName("id") val id: Int,
    @SerializedName("cattle_tag") val cattleTag: String,
    @SerializedName("treatment_type") val treatmentType: String,
    @SerializedName("scheduled_date") val scheduledDate: String,
    @SerializedName("status") val status: String
)
