package com.latihan.si_sapi.models

import com.google.gson.annotations.SerializedName

data class WeightLog(
    val id: Int?,
    @SerializedName("livestock_id")
    val livestockId: Int?,
    val weight: Double,
    val date: String?,
    @SerializedName("worker_id")
    val workerId: Int?
)

data class CreateWeightLogRequest(
    val weight: Double,
    val date: String? = null
)