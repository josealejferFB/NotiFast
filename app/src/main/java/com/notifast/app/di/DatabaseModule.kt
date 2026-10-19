package com.notifast.app.di

import android.content.Context
import androidx.room.Room
import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.data.local.dao.EvaluationDao
import com.notifast.app.data.local.dao.NoteDao
import com.notifast.app.data.local.dao.ScheduleDao
import com.notifast.app.data.local.dao.SubjectDao
import com.notifast.app.data.local.dao.TeacherDao
import com.notifast.app.data.local.dao.UserDao
import com.notifast.app.data.local.migrations.NotiFastMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NotiFastDatabase {
        return Room.databaseBuilder(
            context,
            NotiFastDatabase::class.java,
            "notifast.db"
        ).addMigrations(
            NotiFastMigrations.MIGRATION_1_2,
            NotiFastMigrations.MIGRATION_2_3,
            NotiFastMigrations.MIGRATION_3_4,
            NotiFastMigrations.MIGRATION_4_5,
            NotiFastMigrations.MIGRATION_5_6
        ).build()
    }

    @Provides
    fun provideUserDao(db: NotiFastDatabase): UserDao = db.userDao()

    @Provides
    fun provideSubjectDao(db: NotiFastDatabase): SubjectDao = db.subjectDao()

    @Provides
    fun provideNoteDao(db: NotiFastDatabase): NoteDao = db.noteDao()

    @Provides
    fun provideEvaluationDao(db: NotiFastDatabase): EvaluationDao = db.evaluationDao()

    @Provides
    fun provideScheduleDao(db: NotiFastDatabase): ScheduleDao = db.scheduleDao()

    @Provides
    fun provideTeacherDao(db: NotiFastDatabase): TeacherDao = db.teacherDao()

    @Provides
    fun provideSubjectCloudStateDao(
        db: NotiFastDatabase
    ): com.notifast.app.data.local.dao.SubjectCloudStateDao = db.subjectCloudStateDao()

    @Provides
    fun provideStudyDocumentDao(
        db: NotiFastDatabase
    ): com.notifast.app.data.local.dao.StudyDocumentDao = db.studyDocumentDao()

    @Provides
    fun provideCoverDraftDao(
        db: NotiFastDatabase
    ): com.notifast.app.data.local.dao.CoverDraftDao = db.coverDraftDao()
}
