package com.notifast.app.data.repository

import com.notifast.app.data.local.dao.ScheduleDao
import com.notifast.app.data.local.entity.ScheduleEntity
import com.notifast.app.data.model.Schedule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Date

class LocalScheduleRepositoryImplTest {

    private val scheduleDao: ScheduleDao = mockk(relaxed = true)

    private lateinit var repository: LocalScheduleRepositoryImpl

    @Before
    fun setUp() {
        repository = LocalScheduleRepositoryImpl(scheduleDao = scheduleDao)
    }

    @Test
    fun `getSchedules maps entities to domain Schedule`() = runTest {
        val date = Date()
        val entities = listOf(
            ScheduleEntity(
                id = "s1",
                userId = "u1",
                subjectName = "Algoritmos",
                modality = "presencial",
                dayOfWeek = 1,
                startTime = "08:00",
                endTime = "10:00",
                classroom = "A-101",
                teacher = "Prof. Turing",
                color = 0xFF3B9797,
                createdAt = date,
                updatedAt = date
            )
        )
        coEvery { scheduleDao.getSchedules("u1") } returns flowOf(entities)

        val schedules = repository.getSchedules("u1").first()
        assertEquals(1, schedules.size)
        assertEquals("s1", schedules[0].id)
        assertEquals("Algoritmos", schedules[0].subjectName)
        assertEquals("A-101", schedules[0].classroom)
        assertEquals("Prof. Turing", schedules[0].teacher)
    }

    @Test
    fun `addSchedule generates UUID if id is blank and inserts into dao`() = runTest {
        coEvery { scheduleDao.insertSchedule(any()) } returns 1L

        val schedule = Schedule(
            id = "",
            userId = "u1",
            subjectName = "Física",
            startTime = "10:00",
            endTime = "12:00"
        )
        val result = repository.addSchedule("u1", schedule)
        assertTrue(result.isSuccess)

        coVerify {
            scheduleDao.insertSchedule(match {
                it.userId == "u1" && it.subjectName == "Física" && it.id.isNotBlank()
            })
        }
    }

    @Test
    fun `updateSchedule updates entity in dao`() = runTest {
        coEvery { scheduleDao.updateSchedule(any()) } returns 1

        val schedule = Schedule(
            id = "s1",
            userId = "u1",
            subjectName = "Física II",
            startTime = "14:00",
            endTime = "16:00"
        )
        val result = repository.updateSchedule("u1", schedule)
        assertTrue(result.isSuccess)

        coVerify {
            scheduleDao.updateSchedule(match {
                it.id == "s1" && it.subjectName == "Física II" && it.startTime == "14:00"
            })
        }
    }

    @Test
    fun `deleteSchedule deletes from dao`() = runTest {
        coEvery { scheduleDao.deleteScheduleById("u1", "s1") } returns 1

        val schedule = Schedule(id = "s1", userId = "u1")
        val result = repository.deleteSchedule("u1", schedule)
        assertTrue(result.isSuccess)

        coVerify { scheduleDao.deleteScheduleById("u1", "s1") }
    }
}
