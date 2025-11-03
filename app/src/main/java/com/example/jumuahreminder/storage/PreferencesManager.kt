package com.example.jumuahreminder.storage

import android.content.Context
import android.content.SharedPreferences

/**
 * Manager for handling SharedPreferences storage
 */
class PreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Save prayer times to local storage
     */
    fun savePrayerTimes(
        fajr: String,
        dhuhr: String,
        asr: String,
        maghrib: String,
        isha: String
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_FAJR, fajr)
            putString(KEY_DHUHR, dhuhr)
            putString(KEY_ASR, asr)
            putString(KEY_MAGHRIB, maghrib)
            putString(KEY_ISHA, isha)
            putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Get Maghrib prayer time
     */
    fun getMaghribTime(): String? {
        return sharedPreferences.getString(KEY_MAGHRIB, null)
    }

    /**
     * Get all prayer times
     */
    fun getAllPrayerTimes(): Map<String, String?> {
        return mapOf(
            "Fajr" to sharedPreferences.getString(KEY_FAJR, null),
            "Dhuhr" to sharedPreferences.getString(KEY_DHUHR, null),
            "Asr" to sharedPreferences.getString(KEY_ASR, null),
            "Maghrib" to sharedPreferences.getString(KEY_MAGHRIB, null),
            "Isha" to sharedPreferences.getString(KEY_ISHA, null)
        )
    }

    /**
     * Save user location
     */
    fun saveLocation(latitude: Double, longitude: Double) {
        sharedPreferences.edit().apply {
            putFloat(KEY_LATITUDE, latitude.toFloat())
            putFloat(KEY_LONGITUDE, longitude.toFloat())
            apply()
        }
    }

    /**
     * Get saved location
     */
    fun getLocation(): Pair<Double, Double>? {
        val latitude = sharedPreferences.getFloat(KEY_LATITUDE, 0f)
        val longitude = sharedPreferences.getFloat(KEY_LONGITUDE, 0f)

        return if (latitude != 0f && longitude != 0f) {
            Pair(latitude.toDouble(), longitude.toDouble())
        } else {
            null
        }
    }

    /**
     * Get last update timestamp
     */
    fun getLastUpdateTimestamp(): Long {
        return sharedPreferences.getLong(KEY_LAST_UPDATE, 0L)
    }

    /**
     * Check if prayer times need update (weekly)
     */
    fun needsUpdate(): Boolean {
        val lastUpdate = getLastUpdateTimestamp()
        val currentTime = System.currentTimeMillis()
        val weekInMillis = 7 * 24 * 60 * 60 * 1000L // 7 days

        return (currentTime - lastUpdate) > weekInMillis || lastUpdate == 0L
    }

    /**
     * Save notification preference
     */
    fun setNotificationEnabled(enabled: Boolean) {
        sharedPreferences.edit().apply {
            putBoolean(KEY_NOTIFICATION_ENABLED, enabled)
            apply()
        }
    }

    /**
     * Check if notifications are enabled
     */
    fun isNotificationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }

    /**
     * Save reminder time offset (minutes before Maghrib)
     */
    fun setReminderOffset(minutes: Int) {
        sharedPreferences.edit().apply {
            putInt(KEY_REMINDER_OFFSET, minutes)
            apply()
        }
    }

    /**
     * Get reminder time offset (default 60 minutes before Maghrib)
     */
    fun getReminderOffset(): Int {
        return sharedPreferences.getInt(KEY_REMINDER_OFFSET, 60)
    }

    /**
     * Clear all stored data
     */
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "JumuahReminderPrefs"

        // Keys for prayer times
        private const val KEY_FAJR = "fajr_time"
        private const val KEY_DHUHR = "dhuhr_time"
        private const val KEY_ASR = "asr_time"
        private const val KEY_MAGHRIB = "maghrib_time"
        private const val KEY_ISHA = "isha_time"

        // Keys for location
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"

        // Keys for settings
        private const val KEY_LAST_UPDATE = "last_update"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_REMINDER_OFFSET = "reminder_offset"
    }
}
