package com.endroid.code.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.endroid.code.ThemeMode
import com.endroid.code.data.RecentFilesPrefs
import com.endroid.code.data.SettingsPrefs
import com.endroid.code.editor.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayDeque

enum class Screen { Editor, Settings }

data class EditorUiState(
    val content: String = "",
    val fileName: String = "",
    val fileUri: Uri? = null,
    val isModified: Boolean = false,
    val language: Language = Language.PLAIN,
    val fontSize: Float = 16f,
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = false,
    val keepScreenOn: Boolean = false,
    val showEditorStats: Boolean = true,
    val tabSize: Int = 4,
    val focusEmptyEditor: Boolean = true,
    val caseSensitiveSearch: Boolean = false,
    val goToLine: Int? = null,
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val searchQuery: String = "",
    val searchVisible: Boolean = false,
    val searchMatchIndex: Int = 0,
    val searchMatchCount: Int = 0,
    val statusMessage: String? = null,
    val isLoading: Boolean = false,
    val currentScreen: Screen = Screen.Editor,
    val lineCount: Int = 1,
    val charCount: Int = 0,
    val recentFiles: List<Pair<String, String>> = emptyList()
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = SettingsPrefs(application)
    private val recentPrefs = RecentFilesPrefs(application)

    private val _uiState = MutableStateFlow(
        EditorUiState(
            fontSize = prefs.fontSize,
            showLineNumbers = prefs.showLineNumbers,
            wordWrap = prefs.wordWrap,
            keepScreenOn = prefs.keepScreenOn,
            showEditorStats = prefs.showEditorStats,
            tabSize = prefs.tabSize,
            focusEmptyEditor = prefs.focusEmptyEditor,
            caseSensitiveSearch = prefs.caseSensitiveSearch,
            themeMode = prefs.themeMode,
            recentFiles = recentPrefs.list(),
        )
    )
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<String>(20)
    private val redoStack = ArrayDeque<String>(20)
    private var isUndoRedo = false
    private val maxHistory = 20

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun updateContent(newContent: String) {
        if (isUndoRedo) {
            isUndoRedo = false
            val lines = newContent.count { it == '\n' } + 1
            _uiState.update {
                it.copy(
                    content = newContent,
                    lineCount = lines,
                    charCount = newContent.length
                )
            }
            return
        }
        val current = _uiState.value.content
        if (current != newContent) {
            // Only push undo for non-trivial changes (avoids spam on every keystroke for huge pastes)
            if (kotlin.math.abs(current.length - newContent.length) > 1 || current.take(64) != newContent.take(64)) {
                pushUndo(current)
                redoStack.clear()
            }
            val lines = newContent.count { it == '\n' } + 1
            _uiState.update {
                it.copy(
                    content = newContent,
                    isModified = true,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = false,
                    lineCount = lines,
                    charCount = newContent.length
                )
            }
        }
    }

    private fun pushUndo(content: String) {
        if (undoStack.size >= maxHistory) undoStack.removeFirst()
        undoStack.addLast(content)
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value.content
        redoStack.addLast(current)
        val previous = undoStack.removeLast()
        isUndoRedo = true
        val lines = previous.count { it == '\n' } + 1
        _uiState.update {
            it.copy(
                content = previous,
                isModified = true,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true,
                lineCount = lines,
                charCount = previous.length
            )
        }
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value.content
        undoStack.addLast(current)
        val next = redoStack.removeLast()
        isUndoRedo = true
        val lines = next.count { it == '\n' } + 1
        _uiState.update {
            it.copy(
                content = next,
                isModified = true,
                canUndo = true,
                canRedo = redoStack.isNotEmpty(),
                lineCount = lines,
                charCount = next.length
            )
        }
    }

    fun newFile() {
        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            EditorUiState(
                fontSize = prefs.fontSize,
                showLineNumbers = prefs.showLineNumbers,
                wordWrap = prefs.wordWrap,
                keepScreenOn = prefs.keepScreenOn,
                showEditorStats = prefs.showEditorStats,
                focusEmptyEditor = prefs.focusEmptyEditor,
                caseSensitiveSearch = prefs.caseSensitiveSearch,
                themeMode = prefs.themeMode,
                currentScreen = Screen.Editor
            )
        }
    }

    fun openFile(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val nameHint = getFileName(uri, contentResolver) ?: "file"
            _uiState.update {
                it.copy(
                    isLoading = true,
                    currentScreen = Screen.Editor,
                    statusMessage = "Opening $nameHint…"
                )
            }
            try {
                val name = getFileName(uri, contentResolver) ?: "Unknown"
                // Read on IO; use larger buffer and avoid intermediate StringBuilder growth cost
                val content = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { input ->
                        val limit = 1_048_576 // 1 MB soft limit
                        val bytes = input.readBytes()
                        if (bytes.size > limit) {
                            String(bytes, 0, limit, Charsets.UTF_8) +
                                "\n\n// … truncated for performance (>1 MB) …"
                        } else {
                            String(bytes, Charsets.UTF_8)
                        }
                    } ?: ""
                }
                val lang = Language.fromFileName(name)
                undoStack.clear()
                redoStack.clear()
                val lines = content.count { it == '\n' } + 1
                recentPrefs.add(uri, name)
                _uiState.update {
                    it.copy(
                        content = content,
                        fileName = name,
                        fileUri = uri,
                        isModified = false,
                        language = lang,
                        isLoading = false,
                        canUndo = false,
                        canRedo = false,
                        statusMessage = "Opened $name",
                        lineCount = lines,
                        charCount = content.length,
                        recentFiles = recentPrefs.list()
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, statusMessage = "Error opening file: ${e.message}")
                }
            }
        }
    }

    fun clearRecentFiles() {
        recentPrefs.clear()
        _uiState.update { it.copy(recentFiles = emptyList()) }
    }

    fun reloadFile(contentResolver: ContentResolver) {
        val uri = _uiState.value.fileUri ?: return
        openFile(uri, contentResolver)
    }

    fun needsSaveAs(): Boolean = _uiState.value.fileUri == null

    fun save(contentResolver: ContentResolver) {
        val uri = _uiState.value.fileUri
        if (uri == null) {
            // Caller should route to Save As for new files
            _uiState.update { it.copy(statusMessage = "Choose where to save") }
            return
        }
        saveToUri(uri, contentResolver)
    }

    fun saveToUri(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                withContext(Dispatchers.IO) {
                    contentResolver.openOutputStream(uri, "wt")?.use { out ->
                        out.write(_uiState.value.content.toByteArray(Charsets.UTF_8))
                    } ?: throw Exception("Cannot open output stream")
                }
                val name = getFileName(uri, contentResolver) ?: _uiState.value.fileName
                val lang = Language.fromFileName(name)
                _uiState.update {
                    it.copy(
                        fileUri = uri,
                        fileName = name,
                        language = lang,
                        isModified = false,
                        isLoading = false,
                        statusMessage = "Saved $name"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, statusMessage = "Error saving: ${e.message}")
                }
            }
        }
    }

    fun setLanguage(language: Language) {
        _uiState.update { it.copy(language = language) }
    }

    fun setFontSize(size: Float) {
        val s = size.coerceIn(10f, 28f)
        prefs.fontSize = s
        _uiState.update { it.copy(fontSize = s) }
    }

    fun toggleLineNumbers() {
        val next = !_uiState.value.showLineNumbers
        prefs.showLineNumbers = next
        _uiState.update { it.copy(showLineNumbers = next) }
    }

    fun setShowLineNumbers(show: Boolean) {
        prefs.showLineNumbers = show
        _uiState.update { it.copy(showLineNumbers = show) }
    }

    fun toggleWordWrap() {
        val next = !_uiState.value.wordWrap
        prefs.wordWrap = next
        _uiState.update { it.copy(wordWrap = next) }
    }

    fun setWordWrap(enabled: Boolean) {
        prefs.wordWrap = enabled
        _uiState.update { it.copy(wordWrap = enabled) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        prefs.keepScreenOn = enabled
        _uiState.update { it.copy(keepScreenOn = enabled) }
    }

    fun setTabSize(size: Int) {
        prefs.tabSize = size
        _uiState.update { it.copy(tabSize = prefs.tabSize) }
    }

    fun setShowEditorStats(show: Boolean) {
        prefs.showEditorStats = show
        _uiState.update { it.copy(showEditorStats = show) }
    }

    fun setFocusEmptyEditor(enabled: Boolean) {
        prefs.focusEmptyEditor = enabled
        _uiState.update { it.copy(focusEmptyEditor = enabled) }
    }

    fun setCaseSensitiveSearch(enabled: Boolean) {
        prefs.caseSensitiveSearch = enabled
        _uiState.update {
            val count = countMatches(it.content, it.searchQuery, enabled)
            it.copy(
                caseSensitiveSearch = enabled,
                searchMatchCount = count,
                searchMatchIndex = if (count == 0) 0 else it.searchMatchIndex.coerceIn(0, count - 1)
            )
        }
    }

    fun requestGoToLine(line: Int) {
        _uiState.update { it.copy(goToLine = line.coerceAtLeast(1)) }
    }

    fun clearGoToLine() {
        _uiState.update { it.copy(goToLine = null) }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.themeMode = mode
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setSearchVisible(visible: Boolean) {
        _uiState.update {
            it.copy(
                searchVisible = visible,
                searchQuery = if (!visible) "" else it.searchQuery,
                searchMatchIndex = 0,
                searchMatchCount = if (!visible) 0 else countMatches(it.content, it.searchQuery, it.caseSensitiveSearch)
            )
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update {
            val count = countMatches(it.content, query, it.caseSensitiveSearch)
            it.copy(
                searchQuery = query,
                searchMatchCount = count,
                searchMatchIndex = if (count == 0) 0 else it.searchMatchIndex.coerceIn(0, count - 1)
            )
        }
    }

    fun findNextMatch() {
        _uiState.update {
            if (it.searchMatchCount <= 0) return@update it
            val next = (it.searchMatchIndex + 1) % it.searchMatchCount
            it.copy(searchMatchIndex = next, goToLine = lineOfMatch(it.content, it.searchQuery, next, it.caseSensitiveSearch))
        }
    }

    fun findPreviousMatch() {
        _uiState.update {
            if (it.searchMatchCount <= 0) return@update it
            val prev = (it.searchMatchIndex - 1 + it.searchMatchCount) % it.searchMatchCount
            it.copy(searchMatchIndex = prev, goToLine = lineOfMatch(it.content, it.searchQuery, prev, it.caseSensitiveSearch))
        }
    }

    fun replaceFirst(replacement: String) {
        val s = _uiState.value
        val q = s.searchQuery
        if (q.isEmpty()) return
        val idx = s.content.indexOf(q, ignoreCase = !s.caseSensitiveSearch)
        if (idx < 0) return
        val newContent = s.content.substring(0, idx) + replacement + s.content.substring(idx + q.length)
        updateContent(newContent)
        setSearchQuery(q)
    }

    fun replaceAll(replacement: String) {
        val s = _uiState.value
        val q = s.searchQuery
        if (q.isEmpty()) return
        val regex = if (s.caseSensitiveSearch) Regex(Regex.escape(q)) else Regex(Regex.escape(q), RegexOption.IGNORE_CASE)
        updateContent(regex.replace(s.content, replacement))
        setSearchQuery(q)
    }

    private fun countMatches(content: String, query: String, caseSensitive: Boolean): Int {
        if (query.isEmpty()) return 0
        var count = 0
        var start = 0
        val q = if (caseSensitive) query else query.lowercase()
        val c = if (caseSensitive) content else content.lowercase()
        while (true) {
            val i = c.indexOf(q, start)
            if (i < 0) break
            count++
            start = i + q.length.coerceAtLeast(1)
        }
        return count
    }

    private fun lineOfMatch(
        content: String,
        query: String,
        matchIndex: Int,
        caseSensitive: Boolean
    ): Int {
        if (query.isEmpty() || matchIndex < 0) return 1
        var count = 0
        var start = 0
        val q = if (caseSensitive) query else query.lowercase()
        val c = if (caseSensitive) content else content.lowercase()
        while (true) {
            val i = c.indexOf(q, start)
            if (i < 0) return 1
            if (count == matchIndex) {
                return content.substring(0, i).count { it == '\n' } + 1
            }
            count++
            start = i + q.length.coerceAtLeast(1)
        }
    }

    fun clearStatus() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    private fun getFileName(uri: Uri, contentResolver: ContentResolver): String? {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (cursor.moveToFirst() && nameIndex >= 0) {
                    cursor.getString(nameIndex)
                } else null
            }
        } catch (_: Exception) {
            uri.lastPathSegment
        }
    }
}
