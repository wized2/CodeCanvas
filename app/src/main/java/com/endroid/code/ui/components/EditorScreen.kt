package com.endroid.code.ui.components

import android.content.Intent
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.derivedStateOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.endroid.code.R
import com.endroid.code.editor.Language
import com.endroid.code.editor.SyntaxHighlighter
import com.endroid.code.viewmodel.EditorUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    val snackbarHostState = remember { SnackbarHostState() }
    var menuExpanded by remember { mutableStateOf(false) }
    var showGoToLine by remember { mutableStateOf(false) }
    var goToLineText by remember { mutableStateOf("") }
    var replaceText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

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
                TextButton(onClick = {
                    goToLineText.toIntOrNull()?.let { onGoToLine(it) }
                    showGoToLine = false
                }) { Text("Go") }
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
        containerColor = scheme.surface,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Column(Modifier.padding(end = 4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = state.fileName.ifBlank { "Untitled" },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (state.isModified) {
                                    Box(
                                        Modifier
                                            .padding(start = 8.dp)
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(scheme.tertiary)
                                    )
                                }
                            }
                            Text(
                                text = state.language.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = scheme.surfaceContainer,
                        titleContentColor = scheme.onSurface,
                        actionIconContentColor = scheme.onSurfaceVariant
                    ),
                    actions = {
                        ToolbarIcon(painter = painterResource(R.drawable.ic_save), desc = "Save", onClick = onSave)
                        ToolbarIcon(Icons.Default.Search, "Find") {
                            onSearchVisible(!state.searchVisible)
                        }
                        Box {
                            ToolbarIcon(Icons.Default.MoreVert, "Menu") { menuExpanded = true }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false },
                                modifier = Modifier.widthIn(min = 220.dp)
                            ) {
                                MenuSectionLabel("File")
                                DropdownMenuItem(
                                    text = { Text("New") },
                                    onClick = { menuExpanded = false; onNewFile() }
                                )
                                DropdownMenuItem(
                                    text = { Text("Open") },
                                    onClick = { menuExpanded = false; onOpenFile() }
                                )
                                DropdownMenuItem(
                                    text = { Text("Save As") },
                                    onClick = { menuExpanded = false; onSaveAs() }
                                )
                                if (state.fileUri != null) {
                                    DropdownMenuItem(
                                    text = { Text("Reload from disk") },
                                    onClick = { menuExpanded = false; onReload() }
                                    )
                                }
                                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                MenuSectionLabel("Edit")
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
                                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = { menuExpanded = false; onOpenSettings() }
                                )
                            }
                        }
                    }
                )

                // Find / replace strip
                AnimatedVisibility(
                    visible = state.searchVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        color = scheme.surfaceContainerHigh,
                        tonalElevation = 1.dp
                    ) {
                        Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = state.searchQuery,
                                    onValueChange = onSearchQuery,
                                    placeholder = { Text("Find") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    trailingIcon = {
                                        if (state.searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { onSearchQuery("") }) {
                                                Icon(Icons.Default.Close, "Clear")
                                            }
                                        }
                                    }
                                )
                                TextButton(onClick = onFindPrevious) { Text("Prev") }
                                TextButton(onClick = onFindNext) { Text("Next") }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                OutlinedTextField(
                                    value = replaceText,
                                    onValueChange = { replaceText = it },
                                    placeholder = { Text("Replace") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { onReplaceFirst(replaceText) }) { Text("One") }
                                TextButton(onClick = { onReplaceAll(replaceText) }) { Text("All") }
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                FilterChip(
                                    selected = state.caseSensitiveSearch,
                                    onClick = { onCaseSensitiveSearchChange(!state.caseSensitiveSearch) },
                                    label = { Text("Match case") }
                                )
                                Spacer(Modifier.weight(1f))
                                TextButton(onClick = { onSearchVisible(false) }) { Text("Close") }
                            }
                        }
                    }
                }

                // Recent files strip when welcome
                if (state.content.isEmpty() && state.recentFiles.isNotEmpty()) {
                    Surface(color = scheme.surfaceContainerLowest) {
                        Column(Modifier.padding(vertical = 8.dp)) {
                            Text(
                                "Recent",
                                style = MaterialTheme.typography.labelMedium,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                            )
                            Row(
                                Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                state.recentFiles.take(12).forEach { (uri, name) ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { onOpenRecent(uri) },
                                        label = {
                                            Text(
                                                name,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.widthIn(max = 140.dp)
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(painterResource(R.drawable.ic_folder_open), null, Modifier.size(16.dp))
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = scheme.surfaceContainer,
                tonalElevation = 2.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusBit("Ln ${state.lineCount}")
                    StatusBit(state.language.displayName)
                    StatusBit("UTF-8")
                    if (state.isModified) StatusBit("Modified", emphasize = true)
                    Spacer(Modifier.weight(1f))
                    if (state.isLoading) {
                        CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(scheme.surface)
        ) {
            EditorBody(
                state = state,
                isDark = isSystemInDarkTheme(),
                onContentChange = onContentChange,
                onGoToLineConsumed = onGoToLineConsumed
            )
        }
    }
}

@Composable
private fun MenuSectionLabel(text: String) {
    DropdownMenuItem(
        text = {
            Text(
                text = text.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        },
        onClick = {},
        enabled = false
    )
}

@Composable
private fun StatusBit(text: String, emphasize: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = if (emphasize) MaterialTheme.colorScheme.tertiary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
    )
}


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

    // Debounced highlight: never re-tokenize on every keystroke (main lag fix)
    var highlighted by remember { mutableStateOf(AnnotatedString(state.content)) }
    var isTypingHighlight by remember { mutableStateOf(false) }
    LaunchedEffect(state.content, state.language, isDark) {
        isTypingHighlight = true
        // Plain while typing feels instant
        highlighted = AnnotatedString(state.content)
        delay(220)
        highlighted = withContext(Dispatchers.Default) {
            SyntaxHighlighter.highlight(state.content, state.language, isDark)
        }
        isTypingHighlight = false
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


    val cursorPos by remember {
        derivedStateOf {
            val text = tfState.text.toString()
            val off = tfState.selection.min.coerceIn(0, text.length)
            val before = text.substring(0, off)
            val line = before.count { it == '\n' } + 1
            val lastNl = before.lastIndexOf('\n')
            val col = off - (lastNl + 1) + 1
            line to col.coerceAtLeast(1)
        }
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
            // While typing: single visible layer (no dual paint). Idle: highlight underlay.
            if (!isTypingHighlight) {
                Text(
                    text = highlighted,
                    style = monoStyle,
                    softWrap = state.wordWrap,
                    modifier = Modifier
                )
            }
            BasicTextField(
                state = tfState,
                textStyle = monoStyle.copy(
                    color = if (isTypingHighlight)
                        MaterialTheme.colorScheme.onSurface
                    else
                        androidx.compose.ui.graphics.Color.Transparent
                ),
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
