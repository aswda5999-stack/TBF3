// داخل AtharMuslimApp()
var selectedTheme by remember {
    mutableStateOf(AppThemePreset.fromId(prefs.getString("theme_preset", AppThemePreset.SKY_CLASSIC.id)))
}
var autoSlideEnabled by remember { mutableStateOf(prefs.getBoolean("auto_slide_enabled", true)) }
var showSettingsScreen by remember { mutableStateOf(false) }

// داخل BackHandler(enabled = true) ضع شرط صفحة الإعدادات في البداية:
// showSettingsScreen -> showSettingsScreen = false

// وعند الضغط على زر الإعدادات في SkyHeaderDecor:
// onSettingsClick = { showSettingsScreen = true }

// وعندما تكون showSettingsScreen == true اعرض SettingsScreen(...) كواجهة مستقلة:
// if (showSettingsScreen) {
//     SettingsScreen(
//         selectedTheme = selectedTheme,
//         onSelectTheme = { preset ->
//             selectedTheme = preset
//             prefs.edit().putString("theme_preset", preset.id).apply()
//         },
//         autoSlideEnabled = autoSlideEnabled,
//         onToggleAutoSlide = { enabled ->
//             autoSlideEnabled = enabled
//             prefs.edit().putBoolean("auto_slide_enabled", enabled).apply()
//         },
//         remindersEnabled = remindersEnabled,
//         onToggleReminders = { checked ->
//             remindersEnabled = checked
//             prefs.edit().putBoolean("reminders_enabled", checked).apply()
//             if (checked) AzkarReminderReceiver.scheduleAllDailyReminders(context)
//         },
//         soundEnabled = soundEnabled,
//         onToggleSound = { checked ->
//             soundEnabled = checked
//             prefs.edit().putBoolean("sound_enabled", checked).apply()
//             if (checked) SoundHelper.playTapSound(enabled = true, isCompletion = false)
//         },
//         vibrationEnabled = vibrationEnabled,
//         onToggleVibration = { checked ->
//             vibrationEnabled = checked
//             prefs.edit().putBoolean("vibration_enabled", checked).apply()
//         },
//         fontScaleStep = fontScaleStep,
//         onChangeFontScale = { step ->
//             fontScaleStep = step
//             prefs.edit().putInt("font_scale_step", step).apply()
//         },
//         onResetAllCounts = { resetAllCountsToDefault() },
//         onBack = { showSettingsScreen = false }
//     )
//     return
// }
