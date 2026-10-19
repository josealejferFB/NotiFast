package com.notifast.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.notifast.app.data.local.converter.RoomConverters
import com.notifast.app.data.local.dao.EvaluationDao
import com.notifast.app.data.local.dao.NoteDao
import com.notifast.app.data.local.dao.ScheduleDao
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.data.local.dao.SubjectDao
import com.notifast.app.data.local.dao.TeacherDao
import com.notifast.app.data.local.dao.UserDao
import com.notifast.app.data.local.entity.EvaluationEntity
import com.notifast.app.data.local.dao.CoverDraftDao
import com.notifast.app.data.local.dao.StudyDocumentDao
import com.notifast.app.data.local.entity.CoverDraftEntity
import com.notifast.app.data.local.entity.NoteEntity
import com.notifast.app.data.local.entity.ScheduleEntity
import com.notifast.app.data.local.entity.StudyDocumentEntity
import com.notifast.app.data.local.entity.SubjectCloudStateEntity
import com.notifast.app.data.local.entity.SubjectEntity
import com.notifast.app.data.local.entity.TeacherEntity
import com.notifast.app.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SubjectEntity::class,
        NoteEntity::class,
        EvaluationEntity::class,
        ScheduleEntity::class,
        TeacherEntity::class,
        SubjectCloudStateEntity::class,
        StudyDocumentEntity::class,
        CoverDraftEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class NotiFastDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun subjectDao(): SubjectDao
    abstract fun noteDao(): NoteDao
    abstract fun evaluationDao(): EvaluationDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun teacherDao(): TeacherDao
    abstract fun subjectCloudStateDao(): SubjectCloudStateDao
    abstract fun studyDocumentDao(): StudyDocumentDao
    abstract fun coverDraftDao(): CoverDraftDao
}
