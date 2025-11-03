package com.example.jumuahreminder.data

import com.google.gson.annotations.SerializedName

/**
 * Data classes for Aladhan API response
 */
data class PrayerTimesResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("data")
    val data: PrayerData
)

data class PrayerData(
    @SerializedName("timings")
    val timings: Timings,

    @SerializedName("date")
    val date: DateInfo
)

data class Timings(
    @SerializedName("Fajr")
    val fajr: String,

    @SerializedName("Sunrise")
    val sunrise: String,

    @SerializedName("Dhuhr")
    val dhuhr: String,

    @SerializedName("Asr")
    val asr: String,

    @SerializedName("Maghrib")
    val maghrib: String,

    @SerializedName("Isha")
    val isha: String,

    @SerializedName("Imsak")
    val imsak: String,

    @SerializedName("Midnight")
    val midnight: String,

    @SerializedName("Firstthird")
    val firstthird: String,

    @SerializedName("Lastthird")
    val lastthird: String
)

data class DateInfo(
    @SerializedName("readable")
    val readable: String,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("hijri")
    val hijri: HijriDate,

    @SerializedName("gregorian")
    val gregorian: GregorianDate
)

data class HijriDate(
    @SerializedName("date")
    val date: String,

    @SerializedName("format")
    val format: String,

    @SerializedName("day")
    val day: String,

    @SerializedName("weekday")
    val weekday: WeekdayInfo,

    @SerializedName("month")
    val month: MonthInfo,

    @SerializedName("year")
    val year: String
)

data class GregorianDate(
    @SerializedName("date")
    val date: String,

    @SerializedName("format")
    val format: String,

    @SerializedName("day")
    val day: String,

    @SerializedName("weekday")
    val weekday: WeekdayInfo,

    @SerializedName("month")
    val month: MonthInfo,

    @SerializedName("year")
    val year: String
)

data class WeekdayInfo(
    @SerializedName("en")
    val en: String,

    @SerializedName("ar")
    val ar: String?
)

data class MonthInfo(
    @SerializedName("number")
    val number: Int,

    @SerializedName("en")
    val en: String,

    @SerializedName("ar")
    val ar: String?
)
