package com.notifast.app.di

import com.notifast.app.data.analytics.FirebaseAnalyticsTracker
import com.notifast.app.data.repository.AuthRepositoryImpl
import com.notifast.app.data.repository.ImageRepositoryImpl
import com.notifast.app.data.repository.LocalEvaluationRepositoryImpl
import com.notifast.app.data.repository.LocalNoteRepositoryImpl
import com.notifast.app.data.repository.LocalScheduleRepositoryImpl
import com.notifast.app.data.repository.UserPrefsRepositoryImpl
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.ImageRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.util.AnalyticsTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: LocalNoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindEvaluationRepository(impl: LocalEvaluationRepositoryImpl): EvaluationRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(impl: LocalScheduleRepositoryImpl): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindUserPrefsRepository(impl: UserPrefsRepositoryImpl): UserPrefsRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(impl: FirebaseAnalyticsTracker): AnalyticsTracker
}
