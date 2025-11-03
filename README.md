# تطبيق تذكير صلاة الجمعة / Jumuah Reminder App

<div dir="rtl">

## نظرة عامة

تطبيق Android ذكي يذكرك بصلاة الجمعة قبل ساعة من أذان المغرب. يستخدم التطبيق واجهة برمجة التطبيقات Aladhan للحصول على أوقات الصلاة الدقيقة بناءً على موقعك الجغرافي.

## المميزات الرئيسية

### ✅ المميزات المطلوبة
- 🕌 **أوقات صلاة دقيقة**: استخدام Aladhan API مع طريقة حساب أم القرى (method=4)
- 📍 **تحديد الموقع التلقائي**: الحصول على موقع المستخدم عبر GPS
- ⏰ **تذكير ذكي**: حساب وقت التذكير تلقائياً (قبل المغرب بساعة)
- 🔔 **إشعارات مجدولة**: جدولة الإشعار حسب الوقت الفعلي للمغرب
- 💾 **تخزين محلي**: حفظ أوقات الصلاة محلياً
- 🔄 **تحديث أسبوعي**: تحديث الأوقات تلقائياً كل أسبوع

### ✨ مميزات إضافية
- 🎨 واجهة مستخدم عربية جميلة وسهلة الاستخدام
- 🌙 عرض جميع أوقات الصلاة الخمسة
- 🔕 إمكانية تفعيل/تعطيل الإشعارات
- 📅 عرض وقت آخر تحديث
- 🔄 زر تحديث يدوي
- 🎯 تسليط الضوء على وقت المغرب

## التقنيات المستخدمة

### المكتبات والأدوات
- **Kotlin**: لغة البرمجة الرئيسية
- **Retrofit 2.9.0**: للاتصال بـ API
- **OkHttp3**: لإدارة طلبات الشبكة
- **Gson**: لمعالجة JSON
- **Google Play Services Location**: للحصول على الموقع
- **WorkManager**: للجدولة الأسبوعية
- **AlarmManager**: لجدولة الإشعارات الدقيقة
- **Material Design Components**: لواجهة مستخدم حديثة

### البنية المعمارية
```
com.example.jumuahreminder/
├── data/              # نماذج البيانات من API
├── network/           # طبقة الشبكة (Retrofit)
├── location/          # خدمات الموقع
├── storage/           # التخزين المحلي (SharedPreferences)
├── notification/      # نظام الإشعارات
├── scheduler/         # جدولة المهام
├── worker/            # WorkManager workers
└── MainActivity.kt    # النشاط الرئيسي
```

## متطلبات التشغيل

- Android 7.0 (API level 24) أو أحدث
- أذونات الموقع (GPS)
- الاتصال بالإنترنت (للتحديث الأول)
- إذن الإشعارات (Android 13+)

## الأذونات المطلوبة

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
```

## طريقة التثبيت

### من Android Studio
1. استنساخ المشروع:
```bash
git clone https://github.com/mosalahi/jumuah-reminder.git
cd jumuah-reminder
```

2. فتح المشروع في Android Studio

3. مزامنة Gradle

4. تشغيل التطبيق على جهاز أو محاكي

### بناء APK
```bash
./gradlew assembleRelease
```

## كيفية الاستخدام

1. **التشغيل الأول**:
   - السماح بأذونات الموقع عند الطلب
   - السماح بإذن الإشعارات (Android 13+)
   - اضغط على "تحديث أوقات الصلاة"

2. **التفعيل التلقائي**:
   - قم بتفعيل مفتاح "تفعيل التذكير الأسبوعي"
   - سيتم إرسال تذكير تلقائياً كل يوم جمعة قبل ساعة من المغرب

3. **التحديث اليدوي**:
   - يمكنك تحديث الأوقات يدوياً في أي وقت
   - التحديث التلقائي يحدث أسبوعياً

## API المستخدم

**Aladhan Prayer Times API**
- الرابط: https://api.aladhan.com/v1/timings
- الطريقة: GET
- المعاملات:
  - `latitude`: خط العرض
  - `longitude`: خط الطول
  - `method=4`: طريقة أم القرى للحساب

## الملفات الرئيسية

### الشبكة والـ API
- `AladhanApiService.kt`: واجهة Retrofit للـ API
- `RetrofitClient.kt`: إعداد Retrofit
- `PrayerTimesResponse.kt`: نماذج البيانات

### الموقع
- `LocationHelper.kt`: إدارة خدمات الموقع

### التخزين
- `PreferencesManager.kt`: إدارة SharedPreferences

### الإشعارات والجدولة
- `NotificationHelper.kt`: إدارة الإشعارات
- `JumuahReminderReceiver.kt`: استقبال الإشعارات المجدولة
- `AlarmScheduler.kt`: جدولة المنبهات
- `PrayerTimesUpdateWorker.kt`: عامل التحديث الأسبوعي

## المشاكل المعروفة والحلول

### المشكلة: الإشعار لا يظهر
**الحل**: تأكد من:
- تفعيل الإشعارات في إعدادات التطبيق
- منح إذن POST_NOTIFICATIONS (Android 13+)
- تفعيل التذكير الأسبوعي في التطبيق

### المشكلة: لا يمكن الحصول على الموقع
**الحل**:
- تأكد من تفعيل GPS
- منح أذونات الموقع
- التحقق من اتصال الإنترنت

## المساهمة

المساهمات مرحب بها! يرجى:
1. عمل Fork للمشروع
2. إنشاء فرع للميزة الجديدة
3. تقديم Pull Request

## الترخيص

هذا المشروع مفتوح المصدر ومتاح للاستخدام الحر.

## الاعتمادات

- **Aladhan API**: https://aladhan.com/prayer-times-api
- **Material Design Icons**: Google

## معلومات الاتصال

للأسئلة والاقتراحات، يرجى فتح issue في المستودع.

---

</div>

## Overview (English)

An intelligent Android app that reminds you of Friday prayer one hour before Maghrib. Uses Aladhan API to get accurate prayer times based on your GPS location.

## Key Features

- 🕌 Accurate prayer times using Aladhan API (Umm Al-Qura method)
- 📍 Automatic GPS location detection
- ⏰ Smart reminder (1 hour before Maghrib)
- 🔔 Scheduled notifications for Friday prayers
- 💾 Local storage for prayer times
- 🔄 Automatic weekly updates

## Tech Stack

- Kotlin
- Retrofit for API calls
- Google Play Services Location
- WorkManager for scheduling
- Material Design Components
- SharedPreferences for local storage

## Requirements

- Android 7.0+ (API 24)
- Location permissions
- Internet connection (for initial update)
- Notification permission (Android 13+)

## Installation

```bash
git clone https://github.com/mosalahi/jumuah-reminder.git
cd jumuah-reminder
./gradlew assembleRelease
```

## License

Open source and free to use.
