package com.endroid.code.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.endroid.code.ui.components.EditorScreen
import com.endroid.code.ui.components.SettingsScreen
import com.endroid.code.viewmodel.EditorViewModel
import com.endroid.code.viewmodel.Screen

@Composable
fun CodeCanvasApp(
    viewModel: EditorViewModel,
    onOpenFile: () -> Unit,
    onSaveAs: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatus()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "CodeCanvas" },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { _ ->
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
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onOpenSettings = { viewModel.navigateTo(Screen.Settings) },
                    modifier = Modifier.fillMaxSize()
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
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
