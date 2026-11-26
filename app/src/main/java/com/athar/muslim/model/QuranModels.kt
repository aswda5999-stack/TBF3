package com.athar.muslim.model

data class QuranSurahMeta(
    val number: Int,
    val name: String,
    val ayahCount: Int,
    val revelationType: String
)

data class QuranAyah(
    val id: Int,
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val arabicText: String,
    val tafsirMuyassar: String
)
