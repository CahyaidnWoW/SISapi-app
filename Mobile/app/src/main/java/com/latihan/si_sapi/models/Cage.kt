package com.latihan.si_sapi.models

import com.google.gson.annotations.SerializedName

data class Cage(
    val id: Int,
    val name: String,
    val capacity: Int,
    @SerializedName("current_population")
    val currentPopulation: Int,
    val location: String?
)