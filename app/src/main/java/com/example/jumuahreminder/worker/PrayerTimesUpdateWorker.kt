package com.example.jumuahreminder.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.jumuahreminder.location.LocationHelper
import com.example.jumuahreminder.network.RetrofitClient
import com.example.jumuahreminder.scheduler.AlarmScheduler
import com.example.jumuahreminder.storage.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker to update prayer times weekly
 */
class PrayerTimesUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val preferencesManager = PreferencesManager(context)
    private val locationHelper = LocationHelper(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting prayer times update...")

            // Check if location permission is granted
            if (!locationHelper.hasLocationPermission()) {
                Log.e(TAG, "Location permission not granted")
                return@withContext Result.failure()
            }

            // Get user location
            val location = try {
                locationHelper.getLastKnownLocation() ?: locationHelper.getCurrentLocation()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get location: ${e.message}")
                return@withContext Result.retry()
            }

            // Save location
            preferencesManager.saveLocation(location.latitude, location.longitude)

            // Fetch prayer times from API
            val response = RetrofitClient.apiService.getPrayerTimes(
                latitude = location.latitude,
                longitude = location.longitude,
                method = 4 // Umm Al-Qura method
            )

            if (response.isSuccessful && response.body() != null) {
                val prayerData = response.body()!!.data
                val timings = prayerData.timings

                // Save prayer times to local storage
                preferencesManager.savePrayerTimes(
                    fajr = timings.fajr,
                    dhuhr = timings.dhuhr,
                    asr = timings.asr,
                    maghrib = timings.maghrib,
                    isha = timings.isha
                )

                Log.d(TAG, "Prayer times updated successfully")
                Log.d(TAG, "Maghrib time: ${timings.maghrib}")

                // Schedule Friday reminder
                val alarmScheduler = AlarmScheduler(applicationContext)
                alarmScheduler.scheduleNextFridayReminder()

                Result.success()
            } else {
                Log.e(TAG, "API response failed: ${response.code()}")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating prayer times: ${e.message}", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "PrayerTimesUpdateWorker"
        const val WORK_NAME = "prayer_times_update_work"
    }
}
