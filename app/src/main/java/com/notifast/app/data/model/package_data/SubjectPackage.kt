package com.notifast.app.data.model.package_data

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.data.model.Teacher

data class SubjectPackage(
    val schemaVersion: Int = 1,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val subject: SubjectData,
    val schedules: List<Schedule> = emptyList(),
    val evaluations: List<Evaluation> = emptyList(),
    val notes: List<Note> = emptyList(),
    val teachers: List<Teacher> = emptyList()
)
