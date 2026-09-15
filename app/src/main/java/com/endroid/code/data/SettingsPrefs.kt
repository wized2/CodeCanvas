package com.endroid.code.data

import android.content.Context
import com.endroid.code.ThemeMode

/** Persists editor preferences across process death (SharedPreferences only). */
class SettingsPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var themeMode: ThemeMode
        get() = runCatching {
            ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.AUTO.name)!!)
        }.getOrDefault(ThemeMode.AUTO)
        set(value) = prefs.edit().putString(KEY_THEME, value.name).apply()

    var fontSize: Float
        get() = prefs.getFloat(KEY_FONT_SIZE, 16f).coerceIn(10f, 28f)
        set(value) = prefs.edit().putFloat(KEY_FONT_SIZE, value.coerceIn(10f, 28f)).apply()

    var showLineNumbers: Boolean
        get() = prefs.getBoolean(KEY_LINE_NUMBERS, true)
        set(value) = prefs.edit().putBoolean(KEY_LINE_NUMBERS, value).apply()

    var wordWrap: Boolean
        get() = prefs.getBoolean(KEY_WORD_WRAP, false)
        set(value) = prefs.edit().putBoolean(KEY_WORD_WRAP, value).apply()

    var keepScreenOn: Boolean
        get() = prefs.getBoolean(KEY_KEEP_SCREEN_ON, false)
        set(value) = prefs.edit().putBoolean(KEY_KEEP_SCREEN_ON, value).apply()

    var tabSize: Int
        get() = p.getInt("tab_size", 4).coerceIn(2, 8)
        set(v) = p.edit().putInt("tab_size", v.coerceIn(2, 8)).apply()

    var showEditorStats: Boolean
        get() = prefs.getBoolean(KEY_SHOW_STATS, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_STATS, value).apply()

    var focusEmptyEditor: Boolean
        get() = prefs.getBoolean(KEY_FOCUS_EMPTY, true)
        set(value) = prefs.edit().putBoolean(KEY_FOCUS_EMPTY, value).apply()

    var caseSensitiveSearch: Boolean
        get() = prefs.getBoolean(KEY_CASE_SENSITIVE, false)
        set(value) = prefs.edit().putBoolean(KEY_CASE_SENSITIVE, value).apply()

    companion object {
        private const val PREFS_NAME = "codecanvas_settings"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_FONT_SIZE = "font_size"
        private const val KEY_LINE_NUMBERS = "show_line_numbers"
        private const val KEY_WORD_WRAP = "word_wrap"
        private const val KEY_KEEP_SCREEN_ON = "keep_screen_on"
        private const val KEY_SHOW_STATS = "show_editor_stats"
        private const val KEY_FOCUS_EMPTY = "focus_empty_editor"
        private const val KEY_CASE_SENSITIVE = "case_sensitive_search"
    }
}
