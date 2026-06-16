package com.example.notifast.di

import com.example.notifast.data.repository.AuthRepositoryImpl
import com.example.notifast.data.repository.EvaluationRepositoryImpl
import com.example.notifast.data.repository.ImageRepositoryImpl
import com.example.notifast.data.repository.NoteRepositoryImpl
import com.example.notifast.data.repository.ScheduleRepositoryImpl
import com.example.notifast.domain.repository.AuthRepository
import com.example.notifast.domain.repository.EvaluationRepository
import com.example.notifast.domain.repository.ImageRepository
import com.example.notifast.domain.repository.NoteRepository
import com.example.notifast.domain.repository.ScheduleRepository
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
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindEvaluationRepository(impl: EvaluationRepositoryImpl): EvaluationRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(impl: ImageRepositoryImpl): ImageRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository
}
