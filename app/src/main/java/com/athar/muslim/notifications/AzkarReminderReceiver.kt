package com.athar.muslim.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.athar.muslim.MainActivity
import com.athar.muslim.audio.VoiceAzkarPlayer
import com.athar.muslim.model.AzkarSubCategory
import com.athar.muslim.model.VoiceReminderIntensity
import java.util.Calendar

class AzkarReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences("athar_muslim_prefs", Context.MODE_PRIVATE)
        val isVoiceTick = intent.getBooleanExtra(EXTRA_VOICE_TICK, false)

        if (isVoiceTick) {
            val intensity = VoiceReminderIntensity.fromId(
                prefs.getString("voice_intensity", VoiceReminderIntensity.MEDIUM.id)
            )
            VoiceAzkarPlayer.playNextVoiceReminder(context, intensity)
            scheduleVoiceReminderLoop(context, intensity)
            return
        }

        val categoryKey = intent.getStringExtra(EXTRA_CATEGORY_KEY) ?: AzkarSubCategory.MORNING.key
        val category = AzkarSubCategory.entries.firstOrNull { it.key == categoryKey }
            ?: AzkarSubCategory.MORNING

        ensureNotificationChannel(context)

        if (prefs.getBoolean("reminders_enabled", true)) {
            val hasPostNotif = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

            if (hasPostNotif) {
                val openAppIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val contentPendingIntent = PendingIntent.getActivity(
                    context,
                    category.ordinal + 100,
                    openAppIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_popup_reminder)
                    .setContentTitle("أثر مسلم • ${category.title}")
                    .setContentText(category.subtitle)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(contentPendingIntent)
                    .build()

                try {
                    NotificationManagerCompat.from(context).notify(category.ordinal + 200, notification)
                } catch (_: SecurityException) {
                }
            }
        }

        scheduleCategoryReminder(context, category)
    }

    companion object {
        const val CHANNEL_ID = "athar_muslim_azkar_channel"
        private const val EXTRA_CATEGORY_KEY = "extra_category_key"
        private const val EXTRA_VOICE_TICK = "extra_voice_tick"
        private const val VOICE_ALARM_REQUEST_CODE = 9901

        fun ensureNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "تذكيرات الأذكار اليومية",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "تنبيهات أذكار الصباح والمساء والنوم وبعد الصلاة"
                }
                manager?.createNotificationChannel(channel)
            }
        }

        fun canScheduleExactAlarms(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false
            return alarmManager.canScheduleExactAlarms()
        }

        fun createExactAlarmSettingsIntent(context: Context): Intent? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
            return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
        }

        fun scheduleAllDailyReminders(context: Context) {
            ensureNotificationChannel(context)
            AzkarSubCategory.entries.forEach { category ->
                scheduleCategoryReminder(context, category)
            }
            val prefs = context.getSharedPreferences("athar_muslim_prefs", Context.MODE_PRIVATE)
            if (prefs.getBoolean("voice_azkar_enabled", false)) {
                val intensity = VoiceReminderIntensity.fromId(
                    prefs.getString("voice_intensity", VoiceReminderIntensity.MEDIUM.id)
                )
                scheduleVoiceReminderLoop(context, intensity)
            }
        }

        fun scheduleVoiceReminderLoop(context: Context, intensity: VoiceReminderIntensity) {
            val prefs = context.getSharedPreferences("athar_muslim_prefs", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("voice_azkar_enabled", false)) {
                cancelVoiceReminderLoop(context)
                return
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, AzkarReminderReceiver::class.java).apply {
                putExtra(EXTRA_VOICE_TICK, true)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                VOICE_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val triggerAt = System.currentTimeMillis() + (intensity.intervalMinutes * 60_000L)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAt,
                        pendingIntent
                    )
                }
            } catch (_: Exception) {
            }
        }

        fun cancelVoiceReminderLoop(context: Context) {
            VoiceAzkarPlayer.stopCurrent()
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, AzkarReminderReceiver::class.java).apply {
                putExtra(EXTRA_VOICE_TICK, true)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                VOICE_ALARM_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                alarmManager.cancel(pendingIntent)
            } catch (_: Exception) {
            }
        }

        fun scheduleCategoryReminder(context: Context, category: AzkarSubCategory) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, AzkarReminderReceiver::class.java).apply {
                putExtra(EXTRA_CATEGORY_KEY, category.key)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                category.ordinal + 500,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, category.reminderHour)
                set(Calendar.MINUTE, category.reminderMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } catch (_: SecurityException) {
                try {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } catch (_: Exception) {
                }
            }
        }
    }
}
