package com.example.jumuahreminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.jumuahreminder.storage.PreferencesManager

/**
 * BroadcastReceiver to handle scheduled Jumuah reminders
 */
class JumuahReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val preferencesManager = PreferencesManager(context)

        // Check if notifications are enabled
        if (!preferencesManager.isNotificationEnabled()) {
            return
        }

        // Get Maghrib time
        val maghribTime = preferencesManager.getMaghribTime() ?: return

        // Get reminder offset
        val reminderOffset = preferencesManager.getReminderOffset()

        // Show notification
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showJumuahReminder(maghribTime, reminderOffset)
    }
}
