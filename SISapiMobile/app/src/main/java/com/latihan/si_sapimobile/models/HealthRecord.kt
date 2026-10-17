package com.latihan.si_sapimobile.models

import com.google.gson.annotations.SerializedName

// Bentuk JSON dari GET /health-records/due dan GET /livestocks/{tag}/health-records
// belum dikonfirmasi. Semua field dibuat nullable supaya parsing tidak gagal kalau ada yang beda.
// Kalau daftar pengingat tampil kosong padahal database ada isinya, buka Logcat (filter OkHttp)
// dan kirim JSON-nya, nanti kita sesuaikan @SerializedName.
data class HealthRecord(
    val id: Int = 0,
    @SerializedName("livestock_id") val livestockId: Int? = null,
    val type: String? = null,                 // vaccination | treatment | emergency_report
    val diagnosis: String? = null,
    val treatment: String? = null,
    val date: String? = null,                 // yyyy-MM-dd
    @SerializedName("next_due_date") val nextDueDate: String? = null,
    @SerializedName("photo_path") val photoPath: String? = null,
    @SerializedName("photo_url") val photoUrl: String? = null,
    val livestock: Livestock? = null          // relasi, kalau di-eager-load
)