package com.example.localization

enum class AppLanguage(val code: String, val nativeName: String) {
  ENGLISH("en", "English"),
  BENGALI("bn", "বাংলা");

  companion object {
    fun fromCode(code: String): AppLanguage =
      entries.firstOrNull { it.code == code } ?: ENGLISH
  }
}
