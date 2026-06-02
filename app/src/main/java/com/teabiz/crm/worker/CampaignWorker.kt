package com.teabiz.crm.worker

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.teabiz.crm.TeabizApp
import com.teabiz.crm.data.remote.WhatsAppService
import com.teabiz.crm.data.repository.LeadRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class CampaignWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val leadRepository: LeadRepository,
    private val whatsappService: WhatsAppService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val campaignId = inputData.getString("campaign_id") ?: return Result.failure()

            val campaign = leadRepository.getCampaignById(campaignId) ?: return Result.failure()

            val leads = leadRepository.getWhatsAppOptInLeads().let { flow ->
                // Collect the first emission
                var result = emptyList<com.teabiz.crm.data.model.Lead>()
                flow.take(1).collect { result = it }
                result
            }

            val filteredLeads = if (campaign.targetCategory.isNotBlank()) {
                leads.filter { lead ->
                    lead.productInterest.any { it.contains(campaign.targetCategory, ignoreCase = true) }
                }
            } else {
                leads
            }

            var sentCount = 0
            var failedCount = 0

            for ((index, lead) in filteredLeads.withIndex()) {
                try {
                    if (lead.phone.isBlank()) {
                        failedCount++
                        continue
                    }

                    val personalizedMessage = campaign.messageTemplate
                        .replace("{name}", lead.name)
                        .replace("{company}", lead.company)
                        .replace("{product}", lead.productInterest.joinToString(", "))

                    val result = whatsappService.sendMessage(lead.phone, personalizedMessage)

                    if (result.status == "SENT") {
                        sentCount++
                    } else {
                        failedCount++
                    }

                    setProgress(workDataOf(
                        "current" to index + 1,
                        "total" to filteredLeads.size,
                        "sent" to sentCount,
                        "failed" to failedCount
                    ))

                    TimeUnit.SECONDS.sleep(3) // Rate limiting
                } catch (e: Exception) {
                    failedCount++
                }
            }

            leadRepository.updateCampaign(campaign.copy(
                status = "COMPLETED",
                sentCount = sentCount,
                failedCount = failedCount,
                totalRecipients = filteredLeads.size,
                completedAt = System.currentTimeMillis()
            ))

            showNotification(
                "Campaign Completed",
                "${campaign.name}: $sentCount sent, $failedCount failed"
            )

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(title: String, message: String) {
        val notification = NotificationCompat.Builder(applicationContext, TeabizApp.CAMPAIGN_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    companion object {
        fun run(context: Context, campaignId: String) {
            val inputData = workDataOf("campaign_id" to campaignId)

            val workRequest = OneTimeWorkRequestBuilder<CampaignWorker>()
                .setInputData(inputData)
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
