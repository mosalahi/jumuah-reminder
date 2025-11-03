package com.example.jumuahreminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.jumuahreminder.databinding.ActivityMainBinding
import com.example.jumuahreminder.location.LocationHelper
import com.example.jumuahreminder.network.RetrofitClient
import com.example.jumuahreminder.scheduler.AlarmScheduler
import com.example.jumuahreminder.storage.PreferencesManager
import com.example.jumuahreminder.worker.PrayerTimesUpdateWorker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var locationHelper: LocationHelper
    private lateinit var alarmScheduler: AlarmScheduler

    // Location permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocationGranted && coarseLocationGranted) {
            fetchPrayerTimes()
        } else {
            Toast.makeText(this, "إذن الموقع مطلوب لتحديد أوقات الصلاة", Toast.LENGTH_LONG).show()
        }
    }

    // Notification permission launcher (Android 13+)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Toast.makeText(this, "تم تفعيل الإشعارات", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "الإشعارات معطلة", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize managers
        preferencesManager = PreferencesManager(this)
        locationHelper = LocationHelper(this)
        alarmScheduler = AlarmScheduler(this)

        // Setup UI
        setupUI()

        // Check and request permissions
        checkAndRequestPermissions()

        // Load saved prayer times
        loadPrayerTimes()

        // Setup weekly update worker
        setupWeeklyUpdateWorker()
    }

    private fun setupUI() {
        // Refresh button
        binding.btnRefresh.setOnClickListener {
            if (locationHelper.hasLocationPermission()) {
                fetchPrayerTimes()
            } else {
                requestLocationPermission()
            }
        }

        // Toggle notification switch
        binding.switchNotifications.isChecked = preferencesManager.isNotificationEnabled()
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            preferencesManager.setNotificationEnabled(isChecked)
            if (isChecked) {
                alarmScheduler.scheduleNextFridayReminder()
                Toast.makeText(this, "تم تفعيل التذكير", Toast.LENGTH_SHORT).show()
            } else {
                alarmScheduler.cancelAllReminders()
                Toast.makeText(this, "تم إيقاف التذكير", Toast.LENGTH_SHORT).show()
            }
        }

        // Show last update time
        updateLastUpdateTime()
    }

    private fun checkAndRequestPermissions() {
        // Check location permission
        if (!locationHelper.hasLocationPermission()) {
            requestLocationPermission()
        } else if (preferencesManager.needsUpdate()) {
            fetchPrayerTimes()
        }

        // Check notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(LocationHelper.REQUIRED_PERMISSIONS)
    }

    private fun fetchPrayerTimes() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRefresh.isEnabled = false

        lifecycleScope.launch {
            try {
                // Get user location
                val location = try {
                    locationHelper.getLastKnownLocation() ?: locationHelper.getCurrentLocation()
                } catch (e: Exception) {
                    Toast.makeText(
                        this@MainActivity,
                        "فشل الحصول على الموقع: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    binding.progressBar.visibility = View.GONE
                    binding.btnRefresh.isEnabled = true
                    return@launch
                }

                // Save location
                preferencesManager.saveLocation(location.latitude, location.longitude)

                // Fetch prayer times
                val response = RetrofitClient.apiService.getPrayerTimes(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    method = 4
                )

                if (response.isSuccessful && response.body() != null) {
                    val prayerData = response.body()!!.data
                    val timings = prayerData.timings

                    // Save prayer times
                    preferencesManager.savePrayerTimes(
                        fajr = timings.fajr,
                        dhuhr = timings.dhuhr,
                        asr = timings.asr,
                        maghrib = timings.maghrib,
                        isha = timings.isha
                    )

                    // Update UI
                    loadPrayerTimes()

                    // Schedule Friday reminder
                    if (preferencesManager.isNotificationEnabled()) {
                        alarmScheduler.scheduleNextFridayReminder()
                    }

                    Toast.makeText(this@MainActivity, "تم تحديث أوقات الصلاة", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "فشل تحميل أوقات الصلاة",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching prayer times", e)
                Toast.makeText(
                    this@MainActivity,
                    "خطأ: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.btnRefresh.isEnabled = true
            }
        }
    }

    private fun loadPrayerTimes() {
        val prayerTimes = preferencesManager.getAllPrayerTimes()

        binding.tvFajrTime.text = prayerTimes["Fajr"] ?: "--:--"
        binding.tvDhuhrTime.text = prayerTimes["Dhuhr"] ?: "--:--"
        binding.tvAsrTime.text = prayerTimes["Asr"] ?: "--:--"
        binding.tvMaghribTime.text = prayerTimes["Maghrib"] ?: "--:--"
        binding.tvIshaTime.text = prayerTimes["Isha"] ?: "--:--"

        updateLastUpdateTime()

        // Show/hide empty state
        if (prayerTimes["Maghrib"] == null) {
            binding.layoutPrayerTimes.visibility = View.GONE
            binding.tvEmptyState.visibility = View.VISIBLE
        } else {
            binding.layoutPrayerTimes.visibility = View.VISIBLE
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    private fun updateLastUpdateTime() {
        val lastUpdate = preferencesManager.getLastUpdateTimestamp()
        if (lastUpdate > 0) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvLastUpdate.text = "آخر تحديث: ${sdf.format(Date(lastUpdate))}"
        } else {
            binding.tvLastUpdate.text = "لم يتم التحديث بعد"
        }
    }

    private fun setupWeeklyUpdateWorker() {
        val workRequest = PeriodicWorkRequestBuilder<PrayerTimesUpdateWorker>(
            7, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            PrayerTimesUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
