package com.notifast.app.di

import android.content.Context
import androidx.room.Room
import com.notifast.app.data.local.NotiFastDatabase
import com.notifast.app.data.local.dao.EvaluationDao
import com.notifast.app.data.local.dao.NoteDao
import com.notifast.app.data.local.dao.ScheduleDao
import com.notifast.app.data.local.dao.SubjectDao
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
        ).addMigrations(NotiFastMigrations.MIGRATION_1_2).build()
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
}
