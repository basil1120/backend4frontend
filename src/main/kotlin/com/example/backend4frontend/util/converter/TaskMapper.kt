package com.example.backend4frontend.util.converter

import com.example.backend4frontend.data.dto.TaskCreateRequest
import com.example.backend4frontend.data.dto.TaskFetchResponse
import com.example.backend4frontend.data.domain.entity.Task
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.LocalDateTime


@Component
class TaskMapper {

    fun toDto(entity: Task) = TaskFetchResponse(
        entity.id,
        entity.description,
        entity.isReminderSet,
        entity.isTaskOpen,
        entity.createdOn,
        entity.priority
    )

    fun toEntity(request: TaskCreateRequest, clock: Clock): Task {
        val task = Task()
        task.description = request.description
        task.isReminderSet = request.isReminderSet
        task.isTaskOpen = request.isTaskOpen
        task.createdOn = LocalDateTime.now(clock)
        task.priority = request.priority
        return task
    }
}