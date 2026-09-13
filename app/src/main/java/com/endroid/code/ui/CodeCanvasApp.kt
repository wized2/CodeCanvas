package com.endroid.code.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.endroid.code.ThemeMode
import com.endroid.code.ui.components.EditorScreen
import com.endroid.code.ui.components.SettingsScreen
import com.endroid.code.ui.theme.CodeCanvasTheme
import com.endroid.code.viewmodel.EditorViewModel
import com.endroid.code.viewmodel.Screen

@Composable
fun CodeCanvasApp(
    viewModel: EditorViewModel,
    onOpenFile: () -> Unit,
    onSaveAs: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val keyboard = LocalSoftwareKeyboardController.current

    var showDiscardDialog by remember { mutableStateOf(false) }

    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (uiState.themeMode) {
        ThemeMode.AUTO -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    fun handleBack() {
        when {
            uiState.currentScreen == Screen.Settings -> {
                viewModel.navigateTo(Screen.Editor)
            }
            uiState.isModified -> {
                showDiscardDialog = true
            }
            else -> {
                activity?.finish()
            }
        }
    }

    // System back button + predictive back / gesture
    BackHandler {
        handleBack()
    }

    CodeCanvasTheme(darkTheme = darkTheme, dynamicColor = false) {
        if (showDiscardDialog) {
            AlertDialog(
                onDismissRequest = { showDiscardDialog = false },
                title = { Text("Unsaved changes") },
                text = {
                    Text("You have unsaved edits. Discard them and exit?")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDiscardDialog = false
                            activity?.finish()
                        }
                    ) {
                        Text("Discard")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDiscardDialog = false }) {
                        Text("Keep editing")
                    }
                }
            )
        }

        when (uiState.currentScreen) {
            Screen.Editor -> {
                EditorScreen(
                    state = uiState,
                    onContentChange = viewModel::updateContent,
                    onNewFile = viewModel::newFile,
                    onOpenFile = onOpenFile,
                    onSave = {
                        if (viewModel.needsSaveAs()) {
                            onSaveAs()
                        } else {
                            viewModel.save(context.contentResolver)
                        }
                    },
                    onSaveAs = onSaveAs,
                    onUndo = viewModel::undo,
                    onRedo = viewModel::redo,
                    onSearchVisible = viewModel::setSearchVisible,
                    onSearchQuery = viewModel::setSearchQuery,
                    onOpenSettings = {
                        keyboard?.hide()
                        viewModel.navigateTo(Screen.Settings)
                    },
                    onClearStatus = viewModel::clearStatus,
                )
            }
            Screen.Settings -> {
                SettingsScreen(
                    state = uiState,
                    onBack = { viewModel.navigateTo(Screen.Editor) },
                    onThemeModeChange = viewModel::setThemeMode,
                    onLanguageChange = viewModel::setLanguage,
                    onFontSizeChange = viewModel::setFontSize,
                    onShowLineNumbersChange = viewModel::setShowLineNumbers,
                    onWordWrapChange = viewModel::setWordWrap,
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics { contentDescription = "Settings" }
                )
            }
        }
    }
}
