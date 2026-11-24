package com.athar.muslim.notifications

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.athar.muslim.MainActivity
import com.athar.muslim.model.AzkarSubCategory
import java.util.Calendar

class AzkarReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            scheduleAllDailyReminders(context)
            return
        }

        val categoryKey = intent.getStringExtra(EXTRA_CATEGORY_KEY) ?: AzkarSubCategory.MORNING.key
        val categoryTitle = intent.getStringExtra(EXTRA_CATEGORY_TITLE) ?: "أذكار الصباح"
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)

        showAzkarNotification(
            context = context,
            notificationId = notificationId,
            title = "حان وقت $categoryTitle",
            message = "تذكير يومي: لا تنسَ قراءة $categoryTitle حفظاً وطمأنينة.",
            categoryKey = categoryKey
        )

        // إعادة جدولة التنبيه لليوم التالي
        val matched = AzkarSubCategory.entries.find { it.key == categoryKey }
        if (matched != null) {
            scheduleCategoryReminder(context, matched)
        }
    }

    companion object {
        const val CHANNEL_ID = "athar_muslim_azkar_channel"
        const val EXTRA_CATEGORY_KEY = "extra_category_key"
        const val EXTRA_CATEGORY_TITLE = "extra_category_title"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val existing = manager.getNotificationChannel(CHANNEL_ID)
                if (existing == null) {
                    val channel = NotificationChannel(
                        CHANNEL_ID,
                        "تنبيهات الأذكار اليومية",
                        NotificationManager.IMPORTANCE_HIGH
                    ).apply {
                        description = "إشعارات تذكير أذكار الصباح والمساء والنوم"
                        enableVibration(true)
                        lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                    }
                    manager.createNotificationChannel(channel)
                }
            }
        }

        fun canScheduleExactAlarms(context: Context): Boolean {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                return alarmManager.canScheduleExactAlarms()
            }
            return true
        }

        fun createExactAlarmSettingsIntent(context: Context): Intent? {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            }
            return null
        }

        fun scheduleAllDailyReminders(context: Context) {
            ensureNotificationChannel(context)
            val prefs = context.getSharedPreferences("athar_muslim_prefs", Context.MODE_PRIVATE)
            val remindersEnabled = prefs.getBoolean("reminders_enabled", true)
            if (!remindersEnabled) return

            val reminderCategories = listOf(
                AzkarSubCategory.MORNING,
                AzkarSubCategory.EVENING,
                AzkarSubCategory.SLEEP
            )
            reminderCategories.forEach { cat ->
                scheduleCategoryReminder(context, cat)
            }
        }

        fun scheduleCategoryReminder(context: Context, category: AzkarSubCategory) {
            ensureNotificationChannel(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val now = Calendar.getInstance()
            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, category.reminderHour)
                set(Calendar.MINUTE, category.reminderMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (before(now) || timeInMillis == now.timeInMillis) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val reqCode = when (category) {
                AzkarSubCategory.MORNING -> 2001
                AzkarSubCategory.EVENING -> 2002
                AzkarSubCategory.SLEEP -> 2003
                else -> 2099
            }

            val intent = Intent(context, AzkarReminderReceiver::class.java).apply {
                putExtra(EXTRA_CATEGORY_KEY, category.key)
                putExtra(EXTRA_CATEGORY_TITLE, category.title)
                putExtra(EXTRA_NOTIFICATION_ID, reqCode)
            }

            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getBroadcast(context, reqCode, intent, flags)

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        target.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (_: SecurityException) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    target.timeInMillis,
                    pendingIntent
                )
            }
        }

        fun showAzkarNotification(
            context: Context,
            notificationId: Int,
            title: String,
            message: String,
            categoryKey: String
        ) {
            ensureNotificationChannel(context)

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_CATEGORY_KEY, categoryKey)
            }

            val contentPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(contentPendingIntent)
                .setFullScreenIntent(contentPendingIntent, true)

            try {
                NotificationManagerCompat.from(context).notify(notificationId, builder.build())
            } catch (_: SecurityException) {
                // في حال عدم منح إذن الإشعارات بعد
            }
        }
    }
}
