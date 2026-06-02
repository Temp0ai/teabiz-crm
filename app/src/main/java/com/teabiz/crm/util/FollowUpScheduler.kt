package com.teabiz.crm.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.teabiz.crm.worker.FollowUpWorker

class FollowUpScheduler : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        FollowUpWorker.runOnce(context)
    }
}
