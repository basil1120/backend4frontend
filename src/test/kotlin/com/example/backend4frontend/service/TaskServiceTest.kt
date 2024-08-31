package com.example.backend4frontend.service

import com.example.backend4frontend.errorhandler.BadRequestException
import com.example.backend4frontend.errorhandler.TaskNotFoundException
import com.example.backend4frontend.data.domain.TaskStatus
import com.example.backend4frontend.data.domain.Priority
import com.example.backend4frontend.data.dto.TaskCreateRequest
import com.example.backend4frontend.data.dto.TaskFetchResponse
import com.example.backend4frontend.data.dto.TaskUpdateRequest
import com.example.backend4frontend.data.domain.entity.MAX_DESCRIPTION_LENGTH
import com.example.backend4frontend.data.domain.entity.MIN_DESCRIPTION_LENGTH
import com.example.backend4frontend.data.domain.entity.Task
import com.example.backend4frontend.repository.TaskRepository
import com.example.backend4frontend.util.TaskTimestamp
import com.example.backend4frontend.util.converter.TaskMapper
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(MockKExtension::class)
internal class TaskServiceTest {

    @RelaxedMockK
    private lateinit var mockRepository: TaskRepository

    @RelaxedMockK
    private lateinit var taskTimestamp: TaskTimestamp

    private val taskId: Long = 234
    private val date = LocalDate.of(2020, 12, 31)

    private var mapper = TaskMapper()

    private lateinit var clock: Clock

    private lateinit var task: Task
    private lateinit var createRequest: TaskCreateRequest
    private lateinit var objectUnderTest: TaskService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        createRequest = TaskCreateRequest(
            "test task",
            isReminderSet = false,
            isTaskOpen = false,
            priority = Priority.LOW
        )
        clock = Clock.fixed(date.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
        task = Task()
        objectUnderTest = TaskServiceImpl(mockRepository, mapper, taskTimestamp)
    }

    @Test
    fun `when all tasks get fetched then check if the given size is correct`() {
        val expectedTasks = listOf(Task(), Task())

        every { mockRepository.findAllByOrderByIdAsc() } returns expectedTasks.toMutableSet()
        val actualList: Set<TaskFetchResponse> = objectUnderTest.getTasks(null)

        assertThat(actualList.size).isEqualTo(expectedTasks.size)
    }

    @Test
    fun `when open tasks get fetched then check if the first property has true for isTaskOpen`() {
        task.isTaskOpen = true
        val expectedTasks = listOf(task)

        every { mockRepository.findAllByIsTaskOpenOrderByIdAsc(true) } returns expectedTasks.toMutableSet()
        val actualList: Set<TaskFetchResponse> = objectUnderTest.getTasks(TaskStatus.OPEN)

        assertThat(actualList.elementAt(0).isTaskOpen).isEqualTo(task.isTaskOpen)
    }

    @Test
    fun `when open tasks get fetched then check if the first property has false for isTaskOpen`() {
        task.isTaskOpen = false
        val expectedTasks = listOf(task)

        every { mockRepository.findAllByIsTaskOpenOrderByIdAsc(false) } returns expectedTasks.toMutableSet()
        val actualList: Set<TaskFetchResponse> = objectUnderTest.getTasks(TaskStatus.CLOSED)

        assertThat(actualList.elementAt(0).isTaskOpen).isEqualTo(task.isTaskOpen)
    }

    @Test
    fun `when task gets created then check if it gets properly created`() {
        task = mapper.toEntity(createRequest, taskTimestamp.createClockWithZone())

        every { taskTimestamp.createClockWithZone() } returns Clock.fixed(
            date.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        )
        every { mockRepository.save(any()) } returns task
        val actualTaskFetchDto: TaskFetchResponse = objectUnderTest.createTask(createRequest)

        assertThat(actualTaskFetchDto.id).isEqualTo(task.id)
        assertThat(actualTaskFetchDto.description).isEqualTo(createRequest.description)
        assertThat(actualTaskFetchDto.isReminderSet).isEqualTo(task.isReminderSet)
        assertThat(actualTaskFetchDto.isTaskOpen).isEqualTo(task.isTaskOpen)
        assertThat(actualTaskFetchDto.priority).isEqualTo(task.priority)
        assertThat(actualTaskFetchDto.createdOn).isEqualTo(task.createdOn)
    }

    @Test
    fun `when task gets created with non unique description then check for bad request exception`() {
        every { mockRepository.existsByDescription(any()) } returns true
        val exception = assertThrows<BadRequestException> { objectUnderTest.createTask(createRequest) }

        assertThat(exception.message).isEqualTo("A task with the description '${createRequest.description}' already exists")
        verify { mockRepository.save(any()) wasNot called }
    }

    @Test
    fun `when client wants to create a task with description more than 255 characters then check for bad request exception`() {
        val taskDescriptionTooLong = TaskCreateRequest(
            description = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to,  took a galley of type and scrambled",
            isReminderSet = true,
            isTaskOpen = true,
            priority = Priority.MEDIUM
        )

        val exception = assertThrows<BadRequestException> { objectUnderTest.createTask(taskDescriptionTooLong) }
        assertThat(exception.message).isEqualTo("Description must be between $MIN_DESCRIPTION_LENGTH and $MAX_DESCRIPTION_LENGTH characters in length")
        verify { mockRepository.save(any()) wasNot called }
    }

    @Test
    fun `when client wants to create a task with description less than 3 characters then check for bad request exception`() {
        val taskDescriptionTooShort = TaskCreateRequest(
            description = "ab",
            isReminderSet = false,
            isTaskOpen = false,
            priority = Priority.LOW
        )

        val exception = assertThrows<BadRequestException> { objectUnderTest.createTask(taskDescriptionTooShort) }
        assertThat(exception.message).isEqualTo("Description must be between $MIN_DESCRIPTION_LENGTH and $MAX_DESCRIPTION_LENGTH characters in length")
        verify { mockRepository.save(any()) wasNot called }
    }

    @Test
    fun `when save task is called then check if argument could be captured`() {
        val taskSlot = slot<Task>()
        task.description = createRequest.description
        task.isReminderSet = createRequest.isReminderSet
        task.isTaskOpen = createRequest.isTaskOpen
        task.createdOn = LocalDateTime.now(
            Clock.fixed(date.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
        )
        task.priority = createRequest.priority

        every { taskTimestamp.createClockWithZone() } returns Clock.fixed(
            date.atStartOfDay(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        )
        every { mockRepository.save(capture(taskSlot)) } returns task
        val actualTaskFetchDto: TaskFetchResponse = objectUnderTest.createTask(createRequest)

        verify { mockRepository.save(capture(taskSlot)) }
        assertThat(actualTaskFetchDto.id).isEqualTo(taskSlot.captured.id)
        assertThat(actualTaskFetchDto.description).isEqualTo(taskSlot.captured.description)
        assertThat(actualTaskFetchDto.isReminderSet).isEqualTo(taskSlot.captured.isReminderSet)
        assertThat(actualTaskFetchDto.isTaskOpen).isEqualTo(taskSlot.captured.isTaskOpen)
        assertThat(actualTaskFetchDto.createdOn).isEqualTo(taskSlot.captured.createdOn)
        assertThat(actualTaskFetchDto.priority).isEqualTo(taskSlot.captured.priority)
    }

    @Test
    fun `when get task by id is called then expect a specific description`() {
        task.description = "getTaskById"
        every { mockRepository.existsById(any()) } returns true
        every { mockRepository.findTaskById(any()) } returns task
        val fetchDto = objectUnderTest.getTaskById(1234)

        assertThat(fetchDto.description).isEqualTo(task.description)
    }

    @Test
    fun `when get task by id is called then expect a task not found exception`() {
        every { mockRepository.existsById(any()) } returns false
        val exception = assertThrows<TaskNotFoundException> { objectUnderTest.getTaskById(taskId) }

        assertThat(exception.message).isEqualTo("Task with ID: $taskId does not exist!")
        verify { mockRepository.findTaskById(any()) wasNot called }
    }

    @Test
    fun `when find task by id is called then check if argument could be captured`() {
        val id: Long = 2345
        val taskIdSlot = slot<Long>()

        every { mockRepository.existsById(any()) } returns true
        every { mockRepository.findTaskById(capture(taskIdSlot)) } returns task
        objectUnderTest.getTaskById(id)

        verify { mockRepository.findTaskById(capture(taskIdSlot)) }
        assertThat(taskIdSlot.captured).isEqualTo(id)
    }

    @Test
    fun `when delete task by id is called then check for return message`() {
        every { mockRepository.existsById(any()) } returns true
        val deleteTaskMsg: String = objectUnderTest.deleteTask(taskId)

        assertThat(deleteTaskMsg).isEqualTo("Task with id: $taskId has been deleted.")
    }

    @Test
    fun `when delete by task id is called then check if argument could be captured`() {
        val taskIdSlot = slot<Long>()

        every { mockRepository.existsById(any()) } returns true
        every { mockRepository.deleteById(capture(taskIdSlot)) } returns Unit
        objectUnderTest.deleteTask(taskId)

        verify { mockRepository.deleteById(capture(taskIdSlot)) }
        assertThat(taskIdSlot.captured).isEqualTo(taskId)
    }

    @Test
    fun `when update task is called with task request argument then expect specific description fpr actual task`() {
        task.description = "test task"
        val updateRequest =
            TaskUpdateRequest(
                task.description,
                isReminderSet = false,
                isTaskOpen = false,
                priority = Priority.LOW
            )

        every { mockRepository.existsById(any()) } returns true
        every { mockRepository.findTaskById(any()) } returns task
        every { mockRepository.save(any()) } returns task
        val actualTask = objectUnderTest.updateTask(task.id, updateRequest)

        assertThat(actualTask.description).isEqualTo(task.description)
        assertThat(actualTask.isReminderSet).isEqualTo(task.isReminderSet)
        assertThat(actualTask.isTaskOpen).isEqualTo(task.isTaskOpen)
        assertThat(actualTask.createdOn).isEqualTo(task.createdOn)
        assertThat(actualTask.priority).isEqualTo(task.priority)
    }
}
