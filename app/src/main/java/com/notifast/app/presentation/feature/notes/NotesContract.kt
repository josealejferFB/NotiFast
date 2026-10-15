package com.notifast.app.presentation.feature.notes

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.model.GradeScale

enum class NotesSubTab {
    SUBJECTS,
    PROFESSORS
}

sealed interface NextActivity {
    data class ClassActivity(
        val schedule: Schedule,
        val colorHex: String = ""
    ) : NextActivity

    data class EvaluationActivity(
        val evaluation: Evaluation,
        val colorHex: String = ""
    ) : NextActivity

    data object FreeDay : NextActivity
}

data class SubjectInfo(
    val name: String,
    val icon: String,
    val colorHex: String,
    val notes: List<Note>,
    val evaluations: List<Evaluation>,
    val averageGrade: Double?,
    val completedEvalCount: Int,
    val pendingEvalCount: Int,
    val upcomingEvaluations: List<Evaluation>
)

data class DeleteTarget(
    val subjectName: String,
    val hasCloudCopy: Boolean,
    val driveFileId: String?
)

data class NotesUiState(
    val userId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val notes: Map<String, List<Note>> = emptyMap(),
    val rawNotes: List<Note> = emptyList(),
    val subjects: List<String> = emptyList(),
    val subjectInfos: List<SubjectInfo> = emptyList(),
    val teachers: List<Teacher> = emptyList(),
    val filteredTeachers: List<Teacher> = emptyList(),
    val teacherSearchQuery: String = "",
    val selectedTeacherForDetail: Teacher? = null,
    val teacherBeingEdited: Teacher? = null,
    val selectedSubTab: NotesSubTab = NotesSubTab.SUBJECTS,
    val nextActivity: NextActivity = NextActivity.FreeDay,
    val globalAverage: Double? = null,
    val gradeScale: GradeScale = GradeScale.SCALE_0_10,
    val minPassingGrade: Double = GradeScale.SCALE_0_10.minPassingGrade,
    val totalNotesCount: Int = 0,
    val totalSubjectsCount: Int = 0,
    val searchQuery: String = "",
    val availableTags: List<String> = emptyList(),
    val selectedTag: String? = null,
    val userFirstName: String = "",
    val userLastName: String = "",
    val userEmail: String = "",
    val userProfilePictureUrl: String? = null,
    val syncSummary: com.notifast.app.domain.model.cloud.LocalSyncSummary = com.notifast.app.domain.model.cloud.LocalSyncSummary.NO_ACCOUNT,
    val showCloudHub: Boolean = false,
    val deleteTarget: DeleteTarget? = null,
    val cloudDeleteFailed: Boolean = false
)

sealed interface NotesEvent {
    data object LoadNotes : NotesEvent
    data class Search(val query: String) : NotesEvent
    data class SelectTag(val tag: String?) : NotesEvent
    data class DeleteNote(val note: Note) : NotesEvent
    data class DeleteSubject(val subjectName: String) : NotesEvent
    data class UpdateSubject(val oldName: String, val newName: String, val newColorHex: String, val newIcon: String) : NotesEvent
    data class ToggleTaskCheck(val note: Note, val taskIndex: Int, val isChecked: Boolean) : NotesEvent
    data class SelectSubTab(val subTab: NotesSubTab) : NotesEvent
    data class SearchTeachers(val query: String) : NotesEvent
    data class SelectTeacherForDetail(val teacher: Teacher?) : NotesEvent
    data class OpenAddEditTeacher(val teacher: Teacher?) : NotesEvent
    data class SaveTeacher(val teacher: Teacher) : NotesEvent
    data class DeleteTeacher(val teacherId: String) : NotesEvent
    data object CloudClick : NotesEvent
    data object DismissCloudHub : NotesEvent
    data class RequestDeleteSubject(val subjectName: String) : NotesEvent
    data object CancelDeleteSubject : NotesEvent
    data object DeleteLocalOnly : NotesEvent
    data object DeleteLocalAndCloud : NotesEvent
    data object DeleteFallbackLocal : NotesEvent
}

