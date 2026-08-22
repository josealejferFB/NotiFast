package com.notifast.app.domain.repository

import com.notifast.app.data.model.Schedule
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getSchedules(userId: String): Flow<List<Schedule>>
    suspend fun addSchedule(userId: String, schedule: Schedule): Result<Unit>
    suspend fun updateSchedule(userId: String, schedule: Schedule): Result<Unit>
    suspend fun deleteSchedule(userId: String, schedule: Schedule): Result<Unit>
}
