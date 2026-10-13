package com.notifast.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notifast.app.data.local.entity.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>): List<Long>

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity): Int

    @Query("DELETE FROM schedules WHERE userId = :userId AND id = :scheduleId")
    suspend fun deleteScheduleById(userId: String, scheduleId: String): Int

    @Query("DELETE FROM schedules WHERE userId = :userId AND subjectName = :subjectName")
    suspend fun deleteSchedulesBySubject(userId: String, subjectName: String): Int

    @Query("UPDATE schedules SET subjectName = :newName WHERE userId = :userId AND subjectName = :oldName")
    suspend fun updateSubjectName(userId: String, oldName: String, newName: String): Int

    @Query("DELETE FROM schedules WHERE userId = :userId AND subjectId = :subjectId")
    suspend fun deleteSchedulesBySubjectId(userId: String, subjectId: String): Int

    @Query("DELETE FROM schedules WHERE userId = :userId")
    suspend fun deleteSchedulesByUserId(userId: String): Int

    @Query("SELECT * FROM schedules WHERE userId = :userId ORDER BY dayOfWeek ASC, startTime ASC")
    fun getSchedules(userId: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE userId = :userId AND dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getSchedulesByDay(userId: String, dayOfWeek: Int): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE userId = :userId AND subjectId = :subjectId ORDER BY dayOfWeek ASC, startTime ASC")
    fun getSchedulesBySubjectId(userId: String, subjectId: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE userId = :userId AND id = :scheduleId LIMIT 1")
    suspend fun getScheduleById(userId: String, scheduleId: String): ScheduleEntity?
}
