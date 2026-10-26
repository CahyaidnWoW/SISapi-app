package com.latihan.si_sapimobile.models

import com.google.gson.annotations.SerializedName

data class WorkerTaskResponse(
    @SerializedName("date") val date: String?,
    @SerializedName("total_tasks") val totalTasks: Int = 0,
    @SerializedName("completed_tasks") val completedTasks: Int = 0,
    @SerializedName("tasks") val tasks: List<WorkerTaskItem> = emptyList()
)

data class WorkerTaskItem(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("cage_id") val cageId: Int?,
    @SerializedName("tag_number") val tagNumber: String?,
    @SerializedName("title") val title: String,
    @SerializedName("is_completed") val isCompleted: Boolean,
    @SerializedName("completed_by") val completedBy: String?,
    @SerializedName("completed_at") val completedAt: String?,
    @SerializedName("detail") val detail: String?,
    @SerializedName("action") val action: String?
)
