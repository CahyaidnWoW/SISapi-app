package com.latihan.si_sapimobile.models

import com.google.gson.annotations.SerializedName

data class WeightLog(
    val id: Int? = null,
    @SerializedName("livestock_id") val livestockId: Int? = null,
    val weight: Double? = null,
    val date: String? = null
)

// Backend memakai field `weight` dan `date` (date wajib, format yyyy-MM-dd)
data class CreateWeightLogRequest(
    val weight: Double,
    val date: String
)
