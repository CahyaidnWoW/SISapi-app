package com.latihan.si_sapimobile.models

import com.google.gson.annotations.SerializedName

// Respons asli GET /api/livestocks/{tagNumber} (objek mentah, TIDAK dibungkus success/data):
// { "livestock": {...}, "latest_weight": 342, "latest_weight_date": "2026-09-28",
//   "last_vaccine_date": "...", "next_vaccine_due": "..." }
data class LivestockProfile(
    val livestock: Livestock,
    @SerializedName("latest_weight") val latestWeight: Double? = null,
    @SerializedName("latest_weight_date") val latestWeightDate: String? = null,
    @SerializedName("last_vaccine_date") val lastVaccineDate: String? = null,
    @SerializedName("next_vaccine_due") val nextVaccineDue: String? = null
)

data class Livestock(
    val id: Int = 0,
    @SerializedName("tag_number") val tagNumber: String = "",
    @SerializedName("cage_id") val cageId: Int? = null,
    @SerializedName("breed_id") val breedId: Int? = null,
    val gender: String? = null,
    @SerializedName("initial_weight") val initialWeight: Double? = null,
    val status: String? = null,
    val cage: Cage? = null,
    val breed: Breed? = null
)

// Nama field Cage dan Breed adalah ASUMSI. Kalau di profil tampil "-", lihat JSON asli
// di Logcat (filter: OkHttp) lalu sesuaikan @SerializedName di bawah.
data class Cage(
    val id: Int = 0,
    val name: String? = null
)

data class Breed(
    val id: Int = 0,
    @SerializedName("breed_name") val name: String? = null
)
