package com.example.jumuahreminder.network

import com.example.jumuahreminder.data.PrayerTimesResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit API interface for Aladhan Prayer Times API
 */
interface AladhanApiService {

    /**
     * Get prayer times for a specific location
     * @param latitude Latitude of the location
     * @param longitude Longitude of the location
     * @param method Calculation method (4 = Umm Al-Qura University, Makkah)
     * @param timestamp Unix timestamp for the date (optional)
     */
    @GET("v1/timings")
    suspend fun getPrayerTimes(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 4, // Umm Al-Qura method
        @Query("timestamp") timestamp: Long? = null
    ): Response<PrayerTimesResponse>

    /**
     * Get prayer times for a specific date
     * @param latitude Latitude of the location
     * @param longitude Longitude of the location
     * @param date Date in DD-MM-YYYY format
     * @param method Calculation method (4 = Umm Al-Qura University, Makkah)
     */
    @GET("v1/timings")
    suspend fun getPrayerTimesByDate(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("date") date: String,
        @Query("method") method: Int = 4
    ): Response<PrayerTimesResponse>
}
