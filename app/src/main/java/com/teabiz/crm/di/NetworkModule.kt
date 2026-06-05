package com.teabiz.crm.di

import android.content.Context
import com.teabiz.crm.data.remote.AiService
import com.teabiz.crm.data.remote.AiSalesAssistant
import com.teabiz.crm.data.remote.AiMediaGenerator
import com.teabiz.crm.data.remote.AiVideoGenerator
import com.teabiz.crm.data.remote.GeminiImageGenerator
import com.teabiz.crm.data.remote.Veo3Service
import com.teabiz.crm.data.remote.WhatsAppService
import com.teabiz.crm.data.remote.WhatsAppCatalogFetcher
import com.teabiz.crm.data.remote.SEOService
import com.teabiz.crm.data.remote.GeminiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
    fun provideGeminiService(): GeminiService {
        return GeminiService()
    }

    @Provides
    @Singleton
    fun provideAiService(geminiService: GeminiService): AiService {
        return AiService(geminiService)
    }

    @Provides
    @Singleton
    fun provideSEOService(okHttpClient: OkHttpClient): SEOService {
        return SEOService(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideWhatsAppCatalogFetcher(okHttpClient: OkHttpClient): WhatsAppCatalogFetcher {
        return WhatsAppCatalogFetcher(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideAiSalesAssistant(geminiService: GeminiService): AiSalesAssistant {
        return AiSalesAssistant(geminiService)
    }

    @Provides
    @Singleton
    fun provideAiMediaGenerator(@ApplicationContext context: Context, geminiService: GeminiService): AiMediaGenerator {
        return AiMediaGenerator(context, geminiService)
    }

    @Provides
    @Singleton
    fun provideAiVideoGenerator(@ApplicationContext context: Context, geminiService: GeminiService): AiVideoGenerator {
        return AiVideoGenerator(context, geminiService)
    }

    @Provides
    @Singleton
    fun provideGeminiImageGenerator(@ApplicationContext context: Context, geminiService: GeminiService): GeminiImageGenerator {
        return GeminiImageGenerator(context, geminiService)
    }

    @Provides
    @Singleton
    fun provideVeo3Service(@ApplicationContext context: Context): Veo3Service {
        return Veo3Service(context)
    }
}
