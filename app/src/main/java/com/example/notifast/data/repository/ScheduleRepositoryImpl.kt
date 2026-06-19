package com.example.notifast.data.repository

import com.example.notifast.data.model.Schedule
import com.example.notifast.domain.repository.ScheduleRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepositoryImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ScheduleRepository {

    override fun getSchedules(userId: String): Flow<List<Schedule>> {
        return db.collection("users").document(userId)
            .collection("schedules")
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Schedule::class.java)?.copy(id = doc.id)
                }
            }
            .catch { emit(emptyList()) }
    }

    override suspend fun addSchedule(userId: String, schedule: Schedule): Result<Unit> {
        return try {
            val scheduleMap = mutableMapOf<String, Any>(
                "userId" to schedule.userId,
                "subjectName" to schedule.subjectName,
                "modality" to schedule.modality,
                "dayOfWeek" to schedule.dayOfWeek,
                "startTime" to schedule.startTime,
                "endTime" to schedule.endTime,
                "classroom" to schedule.classroom,
                "teacher" to schedule.teacher,
                "color" to schedule.color,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            if (!schedule.meetingUrl.isNullOrBlank()) {
                scheduleMap["meetingUrl"] = schedule.meetingUrl
            }
            db.collection("users").document(userId)
                .collection("schedules").add(scheduleMap).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateSchedule(userId: String, schedule: Schedule): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "subjectName" to schedule.subjectName,
                "modality" to schedule.modality,
                "dayOfWeek" to schedule.dayOfWeek,
                "startTime" to schedule.startTime,
                "endTime" to schedule.endTime,
                "classroom" to schedule.classroom,
                "teacher" to schedule.teacher,
                "color" to schedule.color,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            if (!schedule.meetingUrl.isNullOrBlank()) {
                updates["meetingUrl"] = schedule.meetingUrl
            }
            db.collection("users").document(userId)
                .collection("schedules").document(schedule.id)
                .update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteSchedule(userId: String, schedule: Schedule): Result<Unit> {
        return try {
            db.collection("users").document(userId)
                .collection("schedules").document(schedule.id)
                .delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
