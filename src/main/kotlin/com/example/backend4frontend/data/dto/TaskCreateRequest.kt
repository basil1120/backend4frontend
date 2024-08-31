package com.example.backend4frontend.data.dto

import com.example.backend4frontend.data.domain.Priority
import com.example.backend4frontend.data.domain.entity.MAX_DESCRIPTION_LENGTH
import com.example.backend4frontend.data.domain.entity.MIN_DESCRIPTION_LENGTH
import jakarta.validation.constraints.Size



data class TaskCreateRequest(
    @Size(min = MIN_DESCRIPTION_LENGTH, max = MAX_DESCRIPTION_LENGTH)
    val description: String,

    val isReminderSet: Boolean,

    val isTaskOpen: Boolean,

    val priority: Priority
)
