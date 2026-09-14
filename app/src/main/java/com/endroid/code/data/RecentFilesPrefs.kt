package com.endroid.code.data

import android.content.Context
import android.net.Uri

/** Last few opened document URIs (Storage Access Framework), newest first. */
class RecentFilesPrefs(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun list(): List<Pair<String, String>> {
        val raw = prefs.getString(KEY, "") ?: return emptyList()
        if (raw.isBlank()) return emptyList()
        return raw.split(SEP)
            .mapNotNull { entry ->
                val parts = entry.split(FIELD, limit = 2)
                if (parts.size == 2 && parts[0].isNotBlank()) parts[0] to parts[1] else null
            }
            .take(MAX)
    }

    fun add(uri: Uri, displayName: String) {
        val name = displayName.ifBlank { uri.lastPathSegment ?: "file" }
        val key = uri.toString()
        val rest = list().filter { it.first != key }
        val next = listOf(key to name) + rest
        val encoded = next.take(MAX).joinToString(SEP) { (u, n) -> u + FIELD + n }
        prefs.edit().putString(KEY, encoded).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        private const val PREFS = "codecanvas_recent"
        private const val KEY = "recent"
        private const val SEP = ""
        private const val FIELD = ""
        private const val MAX = 5
    }
}
