package com.teabiz.crm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.teabiz.crm.data.model.*

@Database(
    entities = [
        Lead::class,
        LeadActivity::class,
        FollowUp::class,
        ImportSession::class,
        Campaign::class,
        SeoKeyword::class,
        Competitor::class,
        ContentCalendar::class,
        GmbPost::class,
        SavedFilter::class,
        AppSetting::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun leadDao(): LeadDao
    abstract fun leadActivityDao(): LeadActivityDao
    abstract fun followUpDao(): FollowUpDao
    abstract fun importSessionDao(): ImportSessionDao
    abstract fun campaignDao(): CampaignDao
    abstract fun seoKeywordDao(): SeoKeywordDao
    abstract fun competitorDao(): CompetitorDao
    abstract fun contentCalendarDao(): ContentCalendarDao
    abstract fun gmbPostDao(): GmbPostDao
    abstract fun savedFilterDao(): SavedFilterDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "teabiz_crm_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
