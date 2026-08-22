package com.notifast.app.data.model

data class SubjectDetailData(
    val subject: SubjectData,
    val notes: List<Note> = emptyList(),
    val evaluations: List<Evaluation> = emptyList(),
    val schedules: List<Schedule> = emptyList(),
    val averageGrade: Double? = null
)
