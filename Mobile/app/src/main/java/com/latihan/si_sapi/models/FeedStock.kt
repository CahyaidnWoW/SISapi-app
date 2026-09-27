package com.latihan.si_sapi.models

import com.google.gson.annotations.SerializedName

data class FeedStock(
    val id: Int,
    @SerializedName("feed_name")
    val feedName: String,
    val unit: String
)

data class CreateFeedConsumptionRequest(
    @SerializedName("feed_id")
    val feedId: Int,
    @SerializedName("amount_used")
    val amountUsed: Double,
    val date: String? = null
)