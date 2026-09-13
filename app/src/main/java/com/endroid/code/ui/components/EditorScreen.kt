package com.endroid.code.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.endroid.code.ThemeMode
import com.endroid.code.editor.Language
import com.endroid.code.editor.SyntaxHighlighter
import com.endroid.code.viewmodel.EditorUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    state: EditorUiState,
    onContentChange: (String) -> Unit,
    onNewFile: () -> Unit,
    onOpenFile: () -> Unit,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onToggleLineNumbers: () -> Unit,
    onToggleWordWrap: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (Language) -> Unit,
    onSearchVisible: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showLangMenu by remember { mutableStateOf(false) }
    var textFieldValue by remember(state.content) {
        mutableStateOf(TextFieldValue(state.content))
    }

    // Keep local TextFieldValue in sync when content changes externally (undo/redo/open)
    if (textFieldValue.text != state.content) {
        textFieldValue = TextFieldValue(state.content, textFieldValue.selection)
    }

    val isDark = when (state.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val highlighted = remember(state.content, state.language, isDark) {
        SyntaxHighlighter.highlight(state.content, state.language, isDark)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = state.fileName.ifBlank { "Untitled" } + if (state.isModified) " •" else "",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.semantics {
                            contentDescription = "Current file: ${state.fileName.ifBlank { "Untitled" }}" +
                                    if (state.isModified) ", modified" else ""
                        }
                    )
                    Text(
                        text = state.language.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = onNewFile,
                    modifier = Modifier.semantics {
                        contentDescription = "New file"
                        role = Role.Button
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            },
            actions = {
                IconButton(
                    onClick = onOpenFile,
                    modifier = Modifier.semantics { contentDescription = "Open file" }
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null)
                }
                IconButton(
                    onClick = onSave,
                    modifier = Modifier.semantics { contentDescription = "Save file" }
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                }
                IconButton(
                    onClick = { onSearchVisible(!state.searchVisible) },
                    modifier = Modifier.semantics { contentDescription = "Toggle search" }
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.semantics { contentDescription = "More options" }
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Save As") },
                        onClick = { showMenu = false; onSaveAs() },
                        leadingIcon = { Icon(Icons.Default.Save, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Undo") },
                        onClick = { showMenu = false; onUndo() },
                        enabled = state.canUndo,
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Undo, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Redo") },
                        onClick = { showMenu = false; onRedo() },
                        enabled = state.canRedo,
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.Redo, null) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text(if (state.showLineNumbers) "Hide Line Numbers" else "Show Line Numbers") },
                        onClick = { showMenu = false; onToggleLineNumbers() }
                    )
                    DropdownMenuItem(
                        text = { Text(if (state.wordWrap) "Disable Word Wrap" else "Enable Word Wrap") },
                        onClick = { showMenu = false; onToggleWordWrap() },
                        leadingIcon = { Icon(Icons.Default.WrapText, null) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Language: ${state.language.displayName}") },
                        onClick = { showMenu = false; showLangMenu = true }
                    )
                    DropdownMenuItem(
                        text = { Text("Theme: ${state.themeMode.name.lowercase().replaceFirstChar { it.uppercase() }}") },
                        onClick = {
                            showMenu = false
                            val next = when (state.themeMode) {
                                ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                ThemeMode.LIGHT -> ThemeMode.DARK
                                ThemeMode.DARK -> ThemeMode.SYSTEM
                            }
                            onThemeModeChange(next)
                        },
                        leadingIcon = {
                            Icon(
                                if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Font Size: ${state.fontSize.toInt()}sp") },
                        onClick = {
                            showMenu = false
                            val next = when {
                                state.fontSize < 14f -> 16f
                                state.fontSize < 18f -> 20f
                                state.fontSize < 24f -> 28f
                                else -> 12f
                            }
                            onFontSizeChange(next)
                        },
                        leadingIcon = { Icon(Icons.Default.FormatSize, null) }
                    )
                }
                DropdownMenu(expanded = showLangMenu, onDismissRequest = { showLangMenu = false }) {
                    Language.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang.displayName) },
                            onClick = {
                                showLangMenu = false
                                onLanguageChange(lang)
                            }
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.semantics { contentDescription = "Editor toolbar" }
        )

        // Search bar
        AnimatedVisibility(
            visible = state.searchVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .semantics { contentDescription = "Search in file" },
                placeholder = { Text("Search…") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { onSearchVisible(false) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close search")
                    }
                }
            )
        }

        // Editor area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                val scrollState = rememberScrollState()
                val horizontalScroll = rememberScrollState()
                val density = LocalDensity.current
                val lineHeight = with(density) { (state.fontSize * 1.45f).sp.toDp() }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Line numbers
                    if (state.showLineNumbers) {
                        val lines = state.content.lines().size.coerceAtLeast(1)
                        Column(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                                .semantics { contentDescription = "Line numbers" },
                            horizontalAlignment = Alignment.End
                        ) {
                            for (i in 1..lines) {
                                Text(
                                    text = i.toString(),
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = (state.fontSize * 0.85f).sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                        lineHeight = (state.fontSize * 1.45f).sp
                                    ),
                                    modifier = Modifier.height(lineHeight)
                                )
                            }
                        }
                    }

                    // Code area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (!state.wordWrap) Modifier.horizontalScroll(horizontalScroll)
                                else Modifier
                            )
                            .padding(12.dp)
                    ) {
                        // Highlighted overlay (visual only)
                        Text(
                            text = highlighted,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = state.fontSize.sp,
                                lineHeight = (state.fontSize * 1.45f).sp,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            softWrap = state.wordWrap,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Transparent editable field on top for input
                        BasicTextField(
                            value = textFieldValue,
                            onValueChange = { newValue ->
                                textFieldValue = newValue
                                onContentChange(newValue.text)
                            },
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = state.fontSize.sp,
                                lineHeight = (state.fontSize * 1.45f).sp,
                                color = androidx.compose.ui.graphics.Color.Transparent // Hide, show highlight below
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            softWrap = state.wordWrap,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "Code editor text area. Current language ${state.language.displayName}"
                                }
                        )
                    }
                }
            }
        }

        // Status bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .semantics { contentDescription = "Status bar" }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${state.content.lines().size} lines  •  ${state.content.length} chars",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = state.language.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
