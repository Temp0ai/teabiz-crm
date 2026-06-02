package com.teabiz.crm.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.teabiz.crm.TeabizApp
import com.teabiz.crm.data.model.FollowUp
import com.teabiz.crm.data.remote.WhatsAppService
import com.teabiz.crm.data.repository.LeadRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class FollowUpWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val leadRepository: LeadRepository,
    private val whatsappService: WhatsAppService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val currentTime = System.currentTimeMillis()
            val pendingFollowUps = leadRepository.getPendingFollowUpsOnce(currentTime)

            var sentCount = 0
            var failedCount = 0

            for (followUp in pendingFollowUps) {
                try {
                    val lead = leadRepository.getLeadById(followUp.leadId) ?: continue
                    if (lead.phone.isBlank()) continue

                    val result = whatsappService.sendMessage(lead.phone, followUp.message)

                    if (result.status == "SENT") {
                        leadRepository.updateFollowUp(followUp.copy(
                            status = "SENT",
                            sentAt = System.currentTimeMillis()
                        ))
                        sentCount++
                    } else {
                        leadRepository.updateFollowUp(followUp.copy(status = "FAILED"))
                        failedCount++
                    }

                    TimeUnit.SECONDS.sleep(3) // Rate limiting
                } catch (e: Exception) {
                    failedCount++
                }
            }

            if (sentCount > 0) {
                showNotification("Follow-ups Sent", "$sentCount follow-up messages sent successfully")
            }
            if (failedCount > 0) {
                showNotification("Follow-up Issues", "$failedCount follow-up messages failed to send")
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(applicationContext, TeabizApp.FOLLOWUP_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        fun schedule(context: Context) {
            val workRequest = PeriodicWorkRequestBuilder<FollowUpWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "follow_up_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun runOnce(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<FollowUpWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
