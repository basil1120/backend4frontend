package com.example.backend4frontend.data.dto

import com.example.backend4frontend.data.domain.Priority

data class TaskUpdateRequest(
    val description: String?,
    val isReminderSet: Boolean?,
    val isTaskOpen: Boolean?,
    val priority: Priority?
)
