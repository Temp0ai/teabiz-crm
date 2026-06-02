package com.teabiz.crm.di

import com.teabiz.crm.data.remote.AiService
import com.teabiz.crm.data.remote.WhatsAppService
import com.teabiz.crm.data.remote.SEOService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideWhatsAppService(okHttpClient: OkHttpClient): WhatsAppService {
        return WhatsAppService(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideAiService(okHttpClient: OkHttpClient): AiService {
        return AiService(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideSEOService(okHttpClient: OkHttpClient): SEOService {
        return SEOService(okHttpClient)
    }
}
