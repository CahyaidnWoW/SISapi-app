package com.latihan.si_sapi.models

import com.google.gson.annotations.SerializedName

data class HealthRecord(
    val id: Int?,
    @SerializedName("livestock_id")
    val livestockId: Int?,
    val type: String, // vaccination, treatment, emergency_report
    val diagnosis: String?,
    val treatment: String?,
    @SerializedName("photo_path")
    val photoPath: String?,
    @SerializedName("next_due_date")
    val nextDueDate: String?,
    @SerializedName("handled_by")
    val handledBy: Int?,
    val date: String?
)

data class CreateHealthReportRequest(
    val type: String = "emergency_report",
    val diagnosis: String,
    val treatment: String? = null
)