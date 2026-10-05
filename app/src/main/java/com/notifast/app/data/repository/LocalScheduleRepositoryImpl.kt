package com.notifast.app.data.repository

import android.util.Log
import com.notifast.app.data.local.dao.ScheduleDao
import com.notifast.app.data.local.entity.ScheduleEntity
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "LocalScheduleRepo"

@Singleton
class LocalScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override fun getSchedules(userId: String): Flow<List<Schedule>> {
        return scheduleDao.getSchedules(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addSchedule(userId: String, schedule: Schedule): Result<Unit> {
        return try {
            val id = if (schedule.id.isBlank()) UUID.randomUUID().toString() else schedule.id
            val now = Date()
            val entity = ScheduleEntity(
                id = id,
                userId = userId,
                subjectName = schedule.subjectName,
                modality = schedule.modality,
                dayOfWeek = schedule.dayOfWeek,
                startTime = schedule.startTime,
                endTime = schedule.endTime,
                classroom = schedule.classroom,
                teacher = schedule.teacher,
                meetingUrl = schedule.meetingUrl,
                color = schedule.color,
                createdAt = schedule.createdAt ?: now,
                updatedAt = now
            )
            scheduleDao.insertSchedule(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add schedule for '${schedule.subjectName}' for user $userId", e)
            Result.failure(e)
        }
    }

    override suspend fun updateSchedule(userId: String, schedule: Schedule): Result<Unit> {
        return try {
            val now = Date()
            val entity = ScheduleEntity(
                id = schedule.id,
                userId = userId,
                subjectName = schedule.subjectName,
                modality = schedule.modality,
                dayOfWeek = schedule.dayOfWeek,
                startTime = schedule.startTime,
                endTime = schedule.endTime,
                classroom = schedule.classroom,
                teacher = schedule.teacher,
                meetingUrl = schedule.meetingUrl,
                color = schedule.color,
                createdAt = schedule.createdAt ?: now,
                updatedAt = now
            )
            scheduleDao.updateSchedule(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update schedule '${schedule.id}'", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteSchedule(userId: String, schedule: Schedule): Result<Unit> {
        return try {
            scheduleDao.deleteScheduleById(userId, schedule.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete schedule '${schedule.id}'", e)
            Result.failure(e)
        }
    }

    private fun ScheduleEntity.toDomain(): Schedule = Schedule(
        id = id,
        userId = userId,
        subjectName = subjectName,
        modality = modality,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime,
        classroom = classroom,
        teacher = teacher,
        meetingUrl = meetingUrl,
        color = color,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
