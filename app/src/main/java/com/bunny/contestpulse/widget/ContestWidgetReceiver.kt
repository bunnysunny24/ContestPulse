package com.bunny.contestpulse.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.bunny.contestpulse.data.sync.ContestSyncWorker

class ContestWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ContestWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_REFRESH) {
            ContestSyncWorker.syncNow(context)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.bunny.contestpulse.REFRESH_WIDGET"
    }
}
