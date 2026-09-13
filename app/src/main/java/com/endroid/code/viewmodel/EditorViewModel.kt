package com.endroid.code.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.endroid.code.ThemeMode
import com.endroid.code.editor.Language
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayDeque

data class EditorUiState(
    val content: String = "",
    val fileName: String = "",
    val fileUri: Uri? = null,
    val isModified: Boolean = false,
    val language: Language = Language.PLAIN,
    val fontSize: Float = 16f,
    val showLineNumbers: Boolean = true,
    val wordWrap: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val searchQuery: String = "",
    val searchVisible: Boolean = false,
    val statusMessage: String? = null,
    val isLoading: Boolean = false
)

class EditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<String>()
    private val redoStack = ArrayDeque<String>()
    private var isUndoRedo = false
    private val maxHistory = 50

    fun updateContent(newContent: String) {
        if (isUndoRedo) {
            isUndoRedo = false
            _uiState.update { it.copy(content = newContent) }
            return
        }
        val current = _uiState.value.content
        if (current != newContent) {
            pushUndo(current)
            redoStack.clear()
            _uiState.update {
                it.copy(
                    content = newContent,
                    isModified = true,
                    canUndo = undoStack.isNotEmpty(),
                    canRedo = false
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
        _uiState.update {
            it.copy(
                content = previous,
                isModified = true,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true
            )
        }
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value.content
        undoStack.addLast(current)
        val next = redoStack.removeLast()
        isUndoRedo = true
        _uiState.update {
            it.copy(
                content = next,
                isModified = true,
                canUndo = true,
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun newFile() {
        undoStack.clear()
        redoStack.clear()
        _uiState.update {
            EditorUiState(
                fontSize = it.fontSize,
                showLineNumbers = it.showLineNumbers,
                wordWrap = it.wordWrap,
                themeMode = it.themeMode
            )
        }
    }

    fun openFile(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val name = getFileName(uri, contentResolver) ?: "Unknown"
                val content = withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() } ?: ""
                }
                val lang = Language.fromFileName(name)
                undoStack.clear()
                redoStack.clear()
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
                        statusMessage = "Opened $name"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, statusMessage = "Error opening file: ${e.message}")
                }
            }
        }
    }

    fun save(contentResolver: ContentResolver) {
        val state = _uiState.value
        val uri = state.fileUri
        if (uri == null) {
            _uiState.update { it.copy(statusMessage = "Use Save As to choose location") }
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
        _uiState.update { it.copy(fontSize = size.coerceIn(10f, 32f)) }
    }

    fun toggleLineNumbers() {
        _uiState.update { it.copy(showLineNumbers = !it.showLineNumbers) }
    }

    fun toggleWordWrap() {
        _uiState.update { it.copy(wordWrap = !it.wordWrap) }
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setSearchVisible(visible: Boolean) {
        _uiState.update { it.copy(searchVisible = visible, searchQuery = if (!visible) "" else it.searchQuery) }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
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
