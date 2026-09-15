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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.VerticalDivider
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
import android.content.Intent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
    onOpenRecent: (String) -> Unit,
    onReload: () -> Unit,
    onSave: () -> Unit,
    onSaveAs: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSearchVisible: (Boolean) -> Unit,
    onSearchQuery: (String) -> Unit,
    onOpenSettings: () -> Unit,
    onClearStatus: () -> Unit,
    onGoToLine: (Int) -> Unit,
    onGoToLineConsumed: () -> Unit,
    onFindNext: () -> Unit,
    onFindPrevious: () -> Unit,
    onReplaceFirst: (String) -> Unit,
    onReplaceAll: (String) -> Unit,
    onCaseSensitiveSearchChange: (Boolean) -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    var menuExpanded by remember { mutableStateOf(false) }
    var showGoToLine by remember { mutableStateOf(false) }
    var goToLineText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(state.statusMessage) {
        val msg = state.statusMessage
        if (!msg.isNullOrBlank() && !state.isLoading) {
            snackbarHostState.showSnackbar(msg)
            onClearStatus()
        }
    }


    if (showGoToLine) {
        AlertDialog(
            onDismissRequest = { showGoToLine = false },
            title = { Text("Go to line") },
            text = {
                OutlinedTextField(
                    value = goToLineText,
                    onValueChange = { goToLineText = it.filter { ch -> ch.isDigit() }.take(7) },
                    label = { Text("Line number") },
                    singleLine = true,
                    modifier = Modifier.semantics { contentDescription = "Line number" }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        goToLineText.toIntOrNull()?.let { onGoToLine(it) }
                        showGoToLine = false
                    }
                ) { Text("Go") }
            },
            dismissButton = {
                TextButton(onClick = { showGoToLine = false }) { Text("Cancel") }
            }
        )
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
                            if (state.fileUri != null) {
                                DropdownMenuItem(
                                    text = { Text("Reload") },
                                    onClick = { menuExpanded = false; onReload() }
                                )
                            }
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
                                text = { Text("Go to line") },
                                onClick = {
                                    menuExpanded = false
                                    goToLineText = ""
                                    showGoToLine = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Copy all") },
                                onClick = {
                                    menuExpanded = false
                                    val cm = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    cm.setPrimaryClip(android.content.ClipData.newPlainText("CodeCanvas", state.content))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    menuExpanded = false
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, state.content)
                                        putExtra(Intent.EXTRA_SUBJECT, state.fileName.ifBlank { "CodeCanvas" })
                                    }
                                    context.startActivity(Intent.createChooser(send, "Share code"))
                                }
                            )
                            if (state.recentFiles.isNotEmpty()) {
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "Recent files (${state.recentFiles.size})",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    onClick = {},
                                    enabled = false
                                )
                                state.recentFiles.forEach { (uri, name) ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                name,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        },
                                        onClick = {
                                            menuExpanded = false
                                            onOpenRecent(uri)
                                        }
                                    )
                                }
                            }
                            HorizontalDivider()
                                                        DropdownMenuItem(
                                text = { Text("Copy all") },
                                onClick = {
                                    menuExpanded = false
                                    clipboard.setText(AnnotatedString(state.content))
                                }
                            )
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
            if (state.recentFiles.isNotEmpty() && !state.searchVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Recent",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    state.recentFiles.forEach { (uri, name) ->
                        TextButton(
                            onClick = { onOpenRecent(uri) },
                            modifier = Modifier.defaultMinSize(minHeight = 36.dp)
                        ) {
                            Text(name, maxLines = 1, softWrap = false)
                        }
                    }
                }
            }
            AnimatedVisibility(
                visible = state.searchVisible,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = onSearchQuery,
                            modifier = Modifier
                                .fillMaxWidth()
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
                        OutlinedTextField(
                            value = replaceText,
                            onValueChange = { replaceText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp)
                                .semantics { contentDescription = "Replace with" },
                            placeholder = { Text("Replace with…") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when {
                                    state.searchQuery.isEmpty() -> "Type to search"
                                    state.searchMatchCount == 0 -> "No matches"
                                    else -> "${state.searchMatchIndex + 1} / ${state.searchMatchCount}"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row {
                                TextButton(
                                    onClick = { onCaseSensitiveSearchChange(!state.caseSensitiveSearch) }
                                ) {
                                    Text(if (state.caseSensitiveSearch) "Aa" else "aa")
                                }
                                TextButton(onClick = onFindPrevious, enabled = state.searchMatchCount > 0) {
                                    Text("Prev")
                                }
                                TextButton(onClick = onFindNext, enabled = state.searchMatchCount > 0) {
                                    Text("Next")
                                }
                                TextButton(
                                    onClick = { onReplaceFirst(replaceText) },
                                    enabled = state.searchMatchCount > 0
                                ) { Text("Replace") }
                                TextButton(
                                    onClick = { onReplaceAll(replaceText) },
                                    enabled = state.searchMatchCount > 0
                                ) { Text("All") }
                            }
                        }
                    }
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
                    onContentChange = onContentChange,
                    onGoToLineConsumed = onGoToLineConsumed
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
                        text = buildString {
                            if (state.showEditorStats) {
                                run {
                                    val words = state.content.split(Regex("\\s+")).count { it.isNotBlank() }
                                    append("${state.lineCount} lines  ·  ${state.charCount} chars  ·  $words words")
                                }
                            } else {
                                append(state.language.displayName)
                            }
                            if (state.keepScreenOn) append("  ·  Screen on")
                            if (state.isModified) append("  ·  Modified")
                        },
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
    onContentChange: (String) -> Unit,
    onGoToLineConsumed: () -> Unit
) {
    val fontSizeSp = state.fontSize.sp
    val lineHeightSp = (state.fontSize * 1.5f).sp
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    val monoStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = fontSizeSp,
        lineHeight = lineHeightSp,
        color = MaterialTheme.colorScheme.onSurface
    )

    // Cap gutter generation — huge files must not build multi-MB line-number strings
    val maxGutterLines = 8_000
    val lineNumbers by remember(state.lineCount, state.showLineNumbers) {
        derivedStateOf {
            if (!state.showLineNumbers || state.lineCount <= 0) return@derivedStateOf ""
            val n = minOf(state.lineCount, maxGutterLines)
            buildString(n * 4) {
                for (i in 1..n) {
                    append(i)
                    if (i < n) append('\n')
                }
                if (state.lineCount > maxGutterLines) {
                    append("\n…")
                }
            }
        }
    }

    val highlighted by remember(state.content, state.language, isDark) {
        derivedStateOf {
            SyntaxHighlighter.highlight(state.content, state.language, isDark)
        }
    }

    val tfState = rememberTextFieldState()
    // Sync external content → field only when it actually differs (open/undo/redo)
    LaunchedEffect(state.content) {
        if (tfState.text.toString() != state.content) {
            tfState.setTextAndPlaceCursorAtEnd(state.content)
        }
    }
    // Field → view model (typing)
    LaunchedEffect(tfState.text) {
        val t = tfState.text.toString()
        if (t != state.content) onContentChange(t)
    }

    LaunchedEffect(state.goToLine) {
        val line = state.goToLine ?: return@LaunchedEffect
        val text = tfState.text.toString()
        val lines = text.split('\n')
        val target = (line - 1).coerceIn(0, (lines.size - 1).coerceAtLeast(0))
        var offset = 0
        for (i in 0 until target) {
            offset += lines[i].length + 1
        }
        offset = offset.coerceIn(0, text.length)
        tfState.edit {
            selection = TextRange(offset)
        }
        onGoToLineConsumed()
    }

    val gutterWidth = when {
        state.lineCount >= 1000 -> 52.dp
        state.lineCount >= 100 -> 40.dp
        else -> 32.dp
    }

    // Shared vertical scroll for gutter + editor; horizontal only on editor when wrap is off.
    // Avoid nesting horizontalScroll+verticalScroll on the same parent (endless scroll / broken axes).
    Row(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (state.showLineNumbers) {
            Box(
                Modifier
                    .width(gutterWidth)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .verticalScroll(vScroll)
                    .padding(top = 8.dp, bottom = 8.dp, start = 6.dp, end = 6.dp)
            ) {
                Text(
                    text = lineNumbers,
                    style = monoStyle.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
                    ),
                    softWrap = false,
                    modifier = Modifier.semantics { contentDescription = "Line numbers" }
                )
            }
            VerticalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        val editorScroll = Modifier
            .weight(1f)
            .fillMaxHeight()
            .verticalScroll(vScroll)
            .then(
                if (!state.wordWrap) Modifier.horizontalScroll(hScroll)
                else Modifier
            )
            .padding(top = 8.dp, bottom = 8.dp, start = 10.dp, end = 12.dp)

        val focusRequester = remember { FocusRequester() }
        val keyboard = LocalSoftwareKeyboardController.current
        val isEmpty = state.content.isEmpty()

        // Empty document: optional auto-focus (Settings → Focus empty editor)
        LaunchedEffect(isEmpty, state.focusEmptyEditor) {
            if (isEmpty && state.focusEmptyEditor) {
                focusRequester.requestFocus()
                keyboard?.show()
            }
        }

        Box(
            editorScroll
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        focusRequester.requestFocus()
                        keyboard?.show()
                    },
                )
        ) {
            // Underlay must use the same wrap rules and style as the input so cursor lines match
            if (isEmpty) {
                Text(
                    text = "Start typing, or open a file.\nTip: overflow menu → Go to line / Share",
                    style = monoStyle.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    ),
                    softWrap = true,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Text(
                text = highlighted,
                style = monoStyle,
                softWrap = state.wordWrap,
                modifier = Modifier
            )
            BasicTextField(
                state = tfState,
                textStyle = monoStyle.copy(color = androidx.compose.ui.graphics.Color.Transparent),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 1),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .then(
                        if (state.wordWrap || isEmpty) Modifier.fillMaxSize()
                        else Modifier
                    )
                    .semantics {
                        contentDescription =
                            "Code editor. Language ${state.language.displayName}"
                    }
            )
        }
    }
}
