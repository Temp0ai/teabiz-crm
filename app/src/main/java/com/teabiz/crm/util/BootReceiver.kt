package com.teabiz.crm.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.teabiz.crm.worker.FollowUpWorker

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            FollowUpWorker.schedule(context)
        }
    }
}
