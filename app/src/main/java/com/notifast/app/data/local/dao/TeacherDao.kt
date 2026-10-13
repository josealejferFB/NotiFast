package com.notifast.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.notifast.app.data.local.entity.TeacherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {

    @Query("SELECT * FROM teachers WHERE userId = :userId ORDER BY firstName ASC, lastName ASC")
    fun getTeachers(userId: String): Flow<List<TeacherEntity>>

    @Query("SELECT * FROM teachers WHERE userId = :userId AND id = :id LIMIT 1")
    fun getTeacherById(userId: String, id: String): Flow<TeacherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherEntity>): List<Long>

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity): Int

    @Query("DELETE FROM teachers WHERE userId = :userId AND id = :id")
    suspend fun deleteTeacher(userId: String, id: String): Int

    @Query("DELETE FROM teachers WHERE userId = :userId")
    suspend fun deleteTeachersByUserId(userId: String): Int
}
