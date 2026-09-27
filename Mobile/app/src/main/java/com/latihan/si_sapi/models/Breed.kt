package com.latihan.si_sapi.models

import com.google.gson.annotations.SerializedName

data class Breed(
    val id: Int,
    @SerializedName("breed_name")
    val breedName: String,
    @SerializedName("ideal_fcr_range")
    val idealFcrRange: String?
)