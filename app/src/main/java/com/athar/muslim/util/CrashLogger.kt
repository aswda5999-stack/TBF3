package com.athar.muslim.util

import android.content.Context
import android.util.Log
import java.io.File

/**
 * يحفظ آخر تقرير انهيار في ملف داخلي ليُعرض في التشغيل التالي،
 * بحيث يسهل تحديد سبب أي توقف مفاجئ.
 */
object CrashLogger {
    private const val FILE_NAME = "last_crash.txt"

    fun install(context: Context) {
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val report = "Thread: ${thread.name}\n" + Log.getStackTraceString(throwable)
                File(appContext.filesDir, FILE_NAME).writeText(report.take(8000))
            } catch (_: Throwable) {
            }
            previous?.uncaughtException(thread, throwable)
        }
    }

    fun readAndClear(context: Context): String? {
        return try {
            val file = File(context.filesDir, FILE_NAME)
            if (!file.exists()) {
                null
            } else {
                val text = file.readText()
                file.delete()
                text.ifBlank { null }
            }
        } catch (_: Throwable) {
            null
        }
    }
}
