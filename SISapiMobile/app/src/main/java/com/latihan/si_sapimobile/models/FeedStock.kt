package com.latihan.si_sapimobile.models

import com.google.gson.annotations.SerializedName

// Respons GET /api/feeds: list mentah. total_stock = SUM(quantity_remaining) semua batch, null kalau belum ada batch.
data class FeedStock(
    val id: Int = 0,
    @SerializedName("feed_name") val feedName: String = "",
    val unit: String? = null,
    @SerializedName("total_stock") val totalStock: Double? = null
)

// POST /api/cages/{id}/feed
// cage_id divalidasi dari BODY (bukan dari URL), dan date WAJIB (yyyy-MM-dd).
data class CreateFeedConsumptionRequest(
    @SerializedName("cage_id") val cageId: Int,
    @SerializedName("feed_id") val feedId: Int,
    @SerializedName("amount_used") val amountUsed: Double,
    val date: String
)
