package com.latihan.si_sapi.models

data class ApiResponse<T>(
    val success: Boolean?,
    val message: String?,
    val data: T?
)