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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.endroid.code.R
import com.endroid.code.ThemeMode
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
    onSearchVisible: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showOverflow by remember { mutableStateOf(false) }

    val isDark = when (state.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    // Highlight only when content / language / theme changes (and under size limit)
    val highlighted = remember(state.content, state.language, isDark) {
        SyntaxHighlighter.highlight(state.content, state.language, isDark)
    }

    // Efficient single-string line numbers (avoids thousands of composables)
    val lineNumbersText = remember(state.lineCount, state.showLineNumbers) {
        if (!state.showLineNumbers || state.lineCount <= 0) ""
        else (1..state.lineCount).joinToString("\n")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = state.fileName.ifBlank { "Untitled" } + if (state.isModified) " •" else "",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = state.language.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
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
                    Icon(painterResource(R.drawable.ic_folder_open), contentDescription = null)
                }
                IconButton(
                    onClick = onSave,
                    modifier = Modifier.semantics { contentDescription = "Save" }
                ) {
                    Icon(painterResource(R.drawable.ic_save), contentDescription = null)
                }
                IconButton(
                    onClick = { onSearchVisible(!state.searchVisible) },
                    modifier = Modifier.semantics { contentDescription = "Search" }
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                }
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.semantics { contentDescription = "Settings" }
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                }
                IconButton(
                    onClick = { showOverflow = true },
                    modifier = Modifier.semantics { contentDescription = "More" }
                ) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(
                    expanded = showOverflow,
                    onDismissRequest = { showOverflow = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Save As") },
                        onClick = { showOverflow = false; onSaveAs() },
                        leadingIcon = { Icon(painterResource(R.drawable.ic_save), null) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Undo") },
                        onClick = { showOverflow = false; onUndo() },
                        enabled = state.canUndo,
                        leadingIcon = { Icon(Icons.Default.KeyboardArrowLeft, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Redo") },
                        onClick = { showOverflow = false; onRedo() },
                        enabled = state.canRedo,
                        leadingIcon = { Icon(Icons.Default.KeyboardArrowRight, null) }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

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
                    .padding(horizontal = 12.dp, vertical = 6.dp)
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

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                val scrollState = rememberScrollState()
                val hScroll = rememberScrollState()
                val fontSizeSp = state.fontSize.sp
                val lineHeightSp = (state.fontSize * 1.4f).sp

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    if (state.showLineNumbers && lineNumbersText.isNotEmpty()) {
                        Text(
                            text = lineNumbersText,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = (state.fontSize * 0.8f).sp,
                                lineHeight = lineHeightSp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                                .widthIn(min = 28.dp)
                                .semantics { contentDescription = "Line numbers" }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (!state.wordWrap) Modifier.horizontalScroll(hScroll)
                                else Modifier
                            )
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        // Syntax-highlighted layer
                        Text(
                            text = highlighted,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = fontSizeSp,
                                lineHeight = lineHeightSp,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            softWrap = state.wordWrap,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Editable transparent layer (new BasicTextField API)
                        val tfState = rememberTextFieldState(state.content)
                        LaunchedEffect(state.content) {
                            if (tfState.text.toString() != state.content) {
                                tfState.setTextAndPlaceCursorAtEnd(state.content)
                            }
                        }
                        LaunchedEffect(tfState.text) {
                            val t = tfState.text.toString()
                            if (t != state.content) onContentChange(t)
                        }
                        BasicTextField(
                            state = tfState,
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = fontSizeSp,
                                lineHeight = lineHeightSp,
                                color = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 1),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription =
                                        "Code editor. Language ${state.language.displayName}"
                                }
                        )
                    }
                }
            }
        }

        // Compact status bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${state.lineCount} lines  ·  ${state.charCount} chars",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
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
