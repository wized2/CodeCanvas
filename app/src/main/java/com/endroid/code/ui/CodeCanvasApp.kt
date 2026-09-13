package com.endroid.code.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (uiState.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.DYNAMIC -> systemDark
    }
    val dynamicColor = uiState.themeMode == ThemeMode.DYNAMIC

    CodeCanvasTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
        when (uiState.currentScreen) {
            Screen.Editor -> {
                EditorScreen(
                    state = uiState,
                    onContentChange = viewModel::updateContent,
                    onNewFile = viewModel::newFile,
                    onOpenFile = onOpenFile,
                    onSave = { viewModel.save(context.contentResolver) },
                    onSaveAs = onSaveAs,
                    onUndo = viewModel::undo,
                    onRedo = viewModel::redo,
                    onSearchVisible = viewModel::setSearchVisible,
                    onSearchQuery = viewModel::setSearchQuery,
                    onOpenSettings = { viewModel.navigateTo(Screen.Settings) },
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
