package com.bunny.contestpulse.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll

object WidgetUpdater {

    private const val TAG = "WidgetUpdater"

    suspend fun updateWidgets(context: Context) {
        try {
            ContestWidget().updateAll(context)
            ContestHeroWidget().updateAll(context)
            Log.d(TAG, "All widgets updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update widgets", e)
        }
    }
}
