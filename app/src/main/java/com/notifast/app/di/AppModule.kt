package com.notifast.app.di

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.notifast.app.data.repository.userPrefsDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics =
        FirebaseAnalytics.getInstance(context)

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.userPrefsDataStore

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): androidx.work.WorkManager =
        androidx.work.WorkManager.getInstance(context)

    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    fun provideOkHttpClient(): okhttp3.OkHttpClient = okhttp3.OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideGson(): com.google.gson.Gson = com.google.gson.Gson()

    @Provides
    @Singleton
    fun provideOpenXmlDocxGenerator(@ApplicationContext context: Context): com.notifast.app.data.study.OpenXmlDocxGenerator =
        com.notifast.app.data.study.OpenXmlDocxGenerator(context)

    @Provides
    @Singleton
    fun provideUnergSchedulePdfParser(): com.notifast.app.domain.util.UnergSchedulePdfParser =
        com.notifast.app.domain.util.UnergSchedulePdfParser()
}
