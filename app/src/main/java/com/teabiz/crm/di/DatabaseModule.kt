package com.teabiz.crm.di

import android.content.Context
import com.teabiz.crm.data.local.AppDatabase
import com.teabiz.crm.data.repository.LeadRepository
import com.teabiz.crm.data.repository.MarketingRepository
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
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideLeadRepository(database: AppDatabase): LeadRepository {
        return LeadRepository(database)
    }

    @Provides
    @Singleton
    fun provideMarketingRepository(database: AppDatabase): MarketingRepository {
        return MarketingRepository(database)
    }
}
