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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.endroid.code.R
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
    onSearchQuery: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onClearStatus: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val snackbarHostState = remember { SnackbarHostState() }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (!msg.isNullOrBlank() && !state.isLoading) {
            snackbarHostState.showSnackbar(msg)
            onClearStatus()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.fileName.ifBlank { "Untitled" },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (state.isModified) {
                            Text(
                                text = "Modified",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                actions = {
                    ToolbarIcon(Icons.Default.Add, "New file", onNewFile)
                    ToolbarIcon(
                        painter = painterResource(R.drawable.ic_folder_open),
                        desc = "Open file",
                        onClick = onOpenFile
                    )
                    ToolbarIcon(
                        painter = painterResource(R.drawable.ic_save),
                        desc = "Save",
                        onClick = onSave
                    )
                    ToolbarIcon(Icons.Default.Search, "Search") {
                        onSearchVisible(!state.searchVisible)
                    }
                    Box {
                        ToolbarIcon(Icons.Default.MoreVert, "More options") {
                            menuExpanded = true
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Save As") },
                                onClick = { menuExpanded = false; onSaveAs() }
                            )
                            DropdownMenuItem(
                                text = { Text("Undo") },
                                enabled = state.canUndo,
                                onClick = { menuExpanded = false; onUndo() }
                            )
                            DropdownMenuItem(
                                text = { Text("Redo") },
                                enabled = state.canRedo,
                                onClick = { menuExpanded = false; onRedo() }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = { menuExpanded = false; onOpenSettings() },
                                leadingIcon = {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedVisibility(
                visible = state.searchVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = onSearchQuery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .semantics { contentDescription = "Search in file" },
                        placeholder = { Text("Find in file…") },
                        singleLine = true,
                        trailingIcon = {
                            IconButton(
                                onClick = { onSearchVisible(false) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close search")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                EditorBody(
                    state = state,
                    isDark = isDark,
                    onContentChange = onContentChange
                )

                if (state.isLoading) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            tonalElevation = 6.dp,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Column(
                                Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = state.statusMessage ?: "Loading…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${state.lineCount} lines  ·  ${state.charCount} chars",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
}

@Composable
private fun ToolbarIcon(
    imageVector: ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .defaultMinSize(48.dp, 48.dp)
            .semantics { contentDescription = desc }
    ) {
        Icon(imageVector, contentDescription = desc, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun ToolbarIcon(
    painter: Painter,
    desc: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .defaultMinSize(48.dp, 48.dp)
            .semantics { contentDescription = desc }
    ) {
        Icon(painter, contentDescription = desc, modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun EditorBody(
    state: EditorUiState,
    isDark: Boolean,
    onContentChange: (String) -> Unit
) {
    val fontSizeSp = state.fontSize.sp
    val lineHeightSp = (state.fontSize * 1.45f).sp
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    val highlighted by remember(state.content, state.language, isDark) {
        derivedStateOf {
            SyntaxHighlighter.highlight(state.content, state.language, isDark)
        }
    }

    val lineNumbers by remember(state.lineCount, state.showLineNumbers) {
        derivedStateOf {
            if (!state.showLineNumbers) ""
            else (1..state.lineCount).joinToString("\n")
        }
    }

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

    val rowModifier = if (state.wordWrap) {
        Modifier
            .fillMaxSize()
            .verticalScroll(vScroll)
            .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
    } else {
        Modifier
            .fillMaxSize()
            .horizontalScroll(hScroll)
            .verticalScroll(vScroll)
            .padding(start = 4.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
    }

    Row(modifier = rowModifier) {
        if (state.showLineNumbers && lineNumbers.isNotEmpty()) {
            Text(
                text = lineNumbers,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSizeSp,
                    lineHeight = lineHeightSp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .padding(end = 10.dp)
                    .widthIn(min = 28.dp)
                    .semantics { contentDescription = "Line numbers" }
            )
        }

        Box(Modifier.weight(1f, fill = false).widthIn(min = 200.dp)) {
            Text(
                text = highlighted,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = fontSizeSp,
                    lineHeight = lineHeightSp
                ),
                softWrap = state.wordWrap
            )
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
