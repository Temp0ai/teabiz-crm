package com.teabiz.crm.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.teabiz.crm.MainActivity
import com.teabiz.crm.data.repository.LeadRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class FollowUpScheduler @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val leadRepository: LeadRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val currentTime = System.currentTimeMillis()
            val dueLeads = leadRepository.getLeadsDueForFollowUp(currentTime).first()

            if (dueLeads.isNotEmpty()) {
                createNotificationChannel()
                for (lead in dueLeads) {
                    showFollowUpNotification(lead.id, lead.name, lead.company)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            FOLLOW_UP_CHANNEL_ID,
            "Follow-up Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for scheduled follow-ups"
        }
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun showFollowUpNotification(leadId: String, leadName: String, company: String) {
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "lead_detail")
            putExtra("lead_id", leadId)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            leadId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val displayText = if (company.isNotBlank()) {
            "Follow up with $leadName ($company)"
        } else {
            "Follow up with $leadName"
        }

        val notification = NotificationCompat.Builder(applicationContext, FOLLOW_UP_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Follow-up Reminder")
            .setContentText(displayText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(leadId.hashCode(), notification)
    }

    companion object {
        const val FOLLOW_UP_CHANNEL_ID = "follow_up_reminders"

        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<FollowUpScheduler>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "follow_up_scheduler",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
