package com.latihan.si_sapi.helpers

import org.json.JSONObject
import retrofit2.Response

/**
 * Ambil pesan error dari body JSON Laravel.
 * - Validasi gagal (422): {"message": "...", "errors": {"field": ["pesan"]}} -> pesan pertama di "errors"
 * - Error biasa: {"message": "Stok pakan tidak mencukupi"} -> isi "message"
 */
fun <T> Response<T>.errorMessage(default: String): String = try {
    val obj = JSONObject(errorBody()?.string().orEmpty())
    val errors = obj.optJSONObject("errors")
    val firstField = errors?.keys()?.asSequence()?.firstOrNull()
    val firstError = firstField?.let { errors.optJSONArray(it)?.optString(0) }
    when {
        !firstError.isNullOrBlank() -> firstError
        obj.optString("message").isNotBlank() -> obj.optString("message")
        else -> default
    }
} catch (e: Exception) {
    default
}
