package com.athar.muslim.model

data class QuranAyah(
    val id: Int,
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val arabicText: String,
    val tafsirMuyassar: String
)
