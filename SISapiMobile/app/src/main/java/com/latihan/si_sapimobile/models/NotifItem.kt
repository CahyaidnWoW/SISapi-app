package com.latihan.si_sapimobile.models

data class NotifItem(
    val title: String,
    val subtitle: String,
    val level: Level,
    val tagNumber: String? = null
) {
    enum class Level { INFO, WARN, DANGER, OK }
}