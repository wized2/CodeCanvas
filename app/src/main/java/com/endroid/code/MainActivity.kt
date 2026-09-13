package com.endroid.code

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.endroid.code.ui.CodeCanvasApp
import com.endroid.code.ui.theme.CodeCanvasTheme
import com.endroid.code.viewmodel.EditorViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: EditorViewModel by viewModels()

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.openFile(it, contentResolver)
        }
    }

    private val createDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/*")
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            viewModel.saveToUri(it, contentResolver)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Handle intent if opened from another app
        handleIncomingIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val darkTheme = when (uiState.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            }

            CodeCanvasTheme(darkTheme = darkTheme) {
                CodeCanvasApp(
                    viewModel = viewModel,
                    onOpenFile = {
                        openDocumentLauncher.launch(arrayOf(
                            "text/*",
                            "application/json",
                            "application/xml",
                            "application/javascript",
                            "application/x-javascript",
                            "*/*"
                        ))
                    },
                    onSaveAs = {
                        val name = viewModel.uiState.value.fileName.ifBlank { "untitled.txt" }
                        createDocumentLauncher.launch(name)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val uri = intent?.data
        if (uri != null && (intent.action == Intent.ACTION_VIEW || intent.action == Intent.ACTION_EDIT)) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Permission may already be granted or not persistable
            }
            viewModel.openFile(uri, contentResolver)
        }
    }
}
