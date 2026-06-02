package com.teabiz.crm

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.teabiz.crm.data.local.AppDatabase
import com.teabiz.crm.worker.FollowUpWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TeaBizApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        FollowUpWorker.schedule(applicationContext)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(NotificationChannel(
                IMPORT_CHANNEL_ID, "Import Progress", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Shows progress of Excel/Gmail import" })

            manager.createNotificationChannel(NotificationChannel(
                FOLLOWUP_CHANNEL_ID, "Follow-up Reminders", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Reminds you about scheduled follow-ups" })

            manager.createNotificationChannel(NotificationChannel(
                CAMPAIGN_CHANNEL_ID, "Campaign Progress", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Shows WhatsApp campaign progress" })

            manager.createNotificationChannel(NotificationChannel(
                AI_CHANNEL_ID, "AI Messages", NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "AI-generated follow-up messages" })
        }
    }

    companion object {
        const val IMPORT_CHANNEL_ID = "import_progress"
        const val FOLLOWUP_CHANNEL_ID = "followup_reminders"
        const val CAMPAIGN_CHANNEL_ID = "campaign_progress"
        const val AI_CHANNEL_ID = "ai_messages"
    }
}
