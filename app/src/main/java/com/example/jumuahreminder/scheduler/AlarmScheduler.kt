package com.example.jumuahreminder.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.jumuahreminder.notification.JumuahReminderReceiver
import com.example.jumuahreminder.storage.PreferencesManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Scheduler for Friday prayer reminders using AlarmManager
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val preferencesManager = PreferencesManager(context)

    /**
     * Schedule the next Friday reminder
     */
    fun scheduleNextFridayReminder() {
        // Get Maghrib time
        val maghribTime = preferencesManager.getMaghribTime()
        if (maghribTime == null) {
            Log.e(TAG, "Maghrib time not available")
            return
        }

        // Get reminder offset (default 60 minutes before Maghrib)
        val reminderOffset = preferencesManager.getReminderOffset()

        // Calculate the reminder time
        val reminderTimeMillis = calculateNextFridayReminderTime(maghribTime, reminderOffset)

        if (reminderTimeMillis == null) {
            Log.e(TAG, "Failed to calculate reminder time")
            return
        }

        // Create pending intent for the alarm
        val intent = Intent(context, JumuahReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule the alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // For Android 6.0+, use setExactAndAllowWhileIdle for exact timing
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTimeMillis,
                    pendingIntent
                )
            }

            val date = Date(reminderTimeMillis)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            Log.d(TAG, "Jumuah reminder scheduled for: ${sdf.format(date)}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to schedule alarm: ${e.message}")
        }
    }

    /**
     * Calculate the next Friday reminder time
     * @param maghribTime Maghrib time in HH:mm format
     * @param minutesBeforeMaghrib Minutes before Maghrib to remind
     * @return Time in milliseconds for the reminder
     */
    private fun calculateNextFridayReminderTime(
        maghribTime: String,
        minutesBeforeMaghrib: Int
    ): Long? {
        try {
            // Parse Maghrib time (format: HH:mm or HH:mm:ss)
            val timeFormat = SimpleDateFormat(
                if (maghribTime.count { it == ':' } == 2) "HH:mm:ss" else "HH:mm",
                Locale.getDefault()
            )
            val maghribDate = timeFormat.parse(maghribTime) ?: return null

            // Get current calendar
            val calendar = Calendar.getInstance()
            val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            // Calculate days until next Friday (Friday = 6 in Calendar)
            val daysUntilFriday = when {
                currentDayOfWeek < Calendar.FRIDAY -> Calendar.FRIDAY - currentDayOfWeek
                currentDayOfWeek == Calendar.FRIDAY -> {
                    // If today is Friday, check if we've passed the reminder time
                    val testCalendar = Calendar.getInstance()
                    testCalendar.set(Calendar.HOUR_OF_DAY, maghribDate.hours)
                    testCalendar.set(Calendar.MINUTE, maghribDate.minutes - minutesBeforeMaghrib)
                    testCalendar.set(Calendar.SECOND, 0)
                    testCalendar.set(Calendar.MILLISECOND, 0)

                    if (calendar.timeInMillis > testCalendar.timeInMillis) {
                        7 // Next week's Friday
                    } else {
                        0 // Today
                    }
                }
                else -> (7 - currentDayOfWeek) + Calendar.FRIDAY
            }

            // Set calendar to next Friday
            calendar.add(Calendar.DAY_OF_YEAR, daysUntilFriday)
            calendar.set(Calendar.HOUR_OF_DAY, maghribDate.hours)
            calendar.set(Calendar.MINUTE, maghribDate.minutes)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            // Subtract the reminder offset
            calendar.add(Calendar.MINUTE, -minutesBeforeMaghrib)

            return calendar.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating reminder time: ${e.message}")
            return null
        }
    }

    /**
     * Cancel all scheduled reminders
     */
    fun cancelAllReminders() {
        val intent = Intent(context, JumuahReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "All reminders cancelled")
    }

    companion object {
        private const val TAG = "AlarmScheduler"
        private const val ALARM_REQUEST_CODE = 1001
    }
}
