package com.latihan.si_sapi.models

import com.google.gson.annotations.SerializedName

data class Livestock(
    val id: Int,
    @SerializedName("tag_number")
    val tagNumber: String,
    @SerializedName("cage_id")
    val cageId: Int,
    @SerializedName("breed_id")
    val breedId: Int,
    val gender: String,
    @SerializedName("birth_date")
    val birthDate: String?,
    @SerializedName("entry_date")
    val entryDate: String?,
    @SerializedName("initial_weight")
    val initialWeight: Double,
    val status: String,
    val cage: Cage?,
    val breed: Breed?
)

data class LivestockProfile(
    val livestock: Livestock,
    @SerializedName("latest_weight")
    val latestWeight: Double?,
    @SerializedName("weight_logs")
    val weightLogs: List<WeightLog>?,
    @SerializedName("health_records")
    val healthRecords: List<HealthRecord>?
)