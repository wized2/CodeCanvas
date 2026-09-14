package com.endroid.code.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.endroid.code.BuildConfig
import com.endroid.code.ThemeMode
import com.endroid.code.editor.Language
import com.endroid.code.viewmodel.EditorUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: EditorUiState,
    onBack: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (Language) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onShowLineNumbersChange: (Boolean) -> Unit,
    onWordWrapChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onShowEditorStatsChange: (Boolean) -> Unit,
    onFocusEmptyEditorChange: (Boolean) -> Unit,
    onCaseSensitiveSearchChange: (Boolean) -> Unit,
    onClearRecent: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmClearRecent by remember { mutableStateOf(false) }

    if (confirmClearRecent) {
        AlertDialog(
            onDismissRequest = { confirmClearRecent = false },
            title = { Text("Clear recent files?") },
            text = { Text("This removes the list of recently opened documents. Files on disk are not deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearRecent()
                    confirmClearRecent = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClearRecent = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(4.dp)
                            .semantics { contentDescription = "Back to editor" }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsSection(title = "Appearance") {
                ThemeDropdown(current = state.themeMode, onSelected = onThemeModeChange)
                Spacer(Modifier.height(8.dp))
                FontSizeSlider(value = state.fontSize, onValueChange = onFontSizeChange)
            }

            SettingsSection(title = "Editor") {
                LanguageDropdown(current = state.language, onSelected = onLanguageChange)
                Spacer(Modifier.height(4.dp))
                SettingsSwitch(
                    title = "Line numbers",
                    checked = state.showLineNumbers,
                    onCheckedChange = onShowLineNumbersChange,
                    description = "Show gutter with line numbers"
                )
                SettingsSwitch(
                    title = "Word wrap",
                    checked = state.wordWrap,
                    onCheckedChange = onWordWrapChange,
                    description = "Wrap long lines instead of horizontal scroll"
                )
                SettingsSwitch(
                    title = "Status bar stats",
                    checked = state.showEditorStats,
                    onCheckedChange = onShowEditorStatsChange,
                    description = "Show line and character counts under the editor"
                )
                SettingsSwitch(
                    title = "Case-sensitive search",
                    checked = state.caseSensitiveSearch,
                    onCheckedChange = onCaseSensitiveSearchChange,
                    description = "Match exact letter case in Find & Replace"
                )
            }

            SettingsSection(title = "Behavior") {
                SettingsSwitch(
                    title = "Keep screen on",
                    checked = state.keepScreenOn,
                    onCheckedChange = onKeepScreenOnChange,
                    description = "Only while editing (clears in Settings, when paused, or when turned off)"
                )
                SettingsSwitch(
                    title = "Focus empty editor",
                    checked = state.focusEmptyEditor,
                    onCheckedChange = onFocusEmptyEditorChange,
                    description = "Open the keyboard automatically on a blank file"
                )
            }

            SettingsSection(title = "Files") {
                Text(
                    text = "Recently opened documents (up to 5) appear in the editor menu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { confirmClearRecent = true },
                    modifier = Modifier.semantics { contentDescription = "Clear recent files" }
                ) {
                    Text("Clear recent files")
                }
            }

            AboutSection()
        }
    }
}

@Composable
private fun AboutSection() {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val versionLabel = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "About",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(vertical = 12.dp)
                        .semantics {
                            contentDescription = if (expanded) "Collapse about" else "Expand about"
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = "CodeCanvas",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = versionLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = if (expanded) "Hide" else "Show",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        Modifier.padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "A lightweight Material 3 code editor for Android. " +
                                "Open and edit local files with syntax highlighting, " +
                                "without accounts or network requirements.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Features",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Syntax highlighting for common languages\n" +
                                "• Line numbers, word wrap, find & replace, undo / redo\n" +
                                "• Storage Access Framework open & save · recent files\n" +
                                "• Material 3 Auto / Light / Dark themes\n" +
                                "• Small release size with R8 + resource shrinking",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "License: free to use and modify. Built with Kotlin & Jetpack Compose.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/wized2/CodeCanvas")
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier.semantics { contentDescription = "Open GitHub repository" }
                        ) {
                            Text("View on GitHub ↗")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    description: String
) {
    Row(
        Modifier
            .fillMaxWidth()
            .semantics { contentDescription = "$title: ${if (checked) "on" else "off"}" },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeDropdown(
    current: ThemeMode,
    onSelected: (ThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (current) {
        ThemeMode.AUTO -> "Auto"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Theme") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .semantics { contentDescription = "Theme: $label" }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ThemeMode.entries.forEach { mode ->
                val itemLabel = when (mode) {
                    ThemeMode.AUTO -> "Auto"
                    ThemeMode.LIGHT -> "Light"
                    ThemeMode.DARK -> "Dark"
                }
                DropdownMenuItem(
                    text = { Text(itemLabel) },
                    onClick = {
                        onSelected(mode)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguageDropdown(
    current: Language,
    onSelected: (Language) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = current.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Language") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .semantics { contentDescription = "Language: ${current.displayName}" }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Language.entries.forEach { lang ->
                DropdownMenuItem(
                    text = { Text(lang.displayName) },
                    onClick = {
                        onSelected(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun FontSizeSlider(
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Text(
            text = "Font size: ${value.toInt()} sp",
            style = MaterialTheme.typography.bodyLarge
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 12f..28f,
            steps = 15,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Font size ${value.toInt()} sp" }
        )
    }
}
