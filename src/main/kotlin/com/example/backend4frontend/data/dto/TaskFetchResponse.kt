package com.example.backend4frontend.data.dto

import com.example.backend4frontend.data.domain.Priority
import java.time.LocalDateTime

data class TaskFetchResponse(
    val id: Long,
    val description: String,
    val isReminderSet: Boolean,
    val isTaskOpen: Boolean,
    val createdOn: LocalDateTime,
    val priority: Priority
)
