# CodeCanvas

**A sleek, accessible code editor for Android** built with **Kotlin** and **Jetpack Compose**.

![Min API 26](https://img.shields.io/badge/minSdk-26-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-purple)
![License](https://img.shields.io/badge/license-MIT-lightgrey)

CodeCanvas is a modern, lightweight mobile code editor focused on a clean UI, good accessibility, and practical everyday coding on the go.

## Features

- **Syntax highlighting** for many languages:
  - Kotlin, Java, Python, JavaScript, TypeScript
  - XML / HTML, JSON, CSS, Markdown, SQL, Shell, C/C++, Rust, Go
- **Line numbers** (toggleable)
- **Word wrap** (toggleable)
- **Undo / Redo** history
- **Search** in current file
- **Open / Save / Save As** using the system Storage Access Framework (works with any file provider)
- **Open from other apps** (supports `text/*`, JSON, XML, JS via intent filters)
- **Theme modes**: System / Light / Dark (Tokyo Night inspired dark palette)
- **Adjustable font size**
- **Language detection** from file extension + manual override
- **Status bar** with line/char count
- **Sleek Material 3 UI** with high contrast and semantic accessibility labels
- **Edge-to-edge** design

## Screenshots & Design

The UI uses a Tokyo Night inspired color scheme for the dark theme and a clean light theme. The editor is monospace, with subtle line-number gutters and a minimal status bar so the focus stays on your code.

Accessibility is taken seriously:
- Content descriptions on all interactive elements
- Semantic roles
- Sufficient contrast
- Support for system font scaling (via Compose)
- Keyboard / IME friendly layout

## Requirements

- Android 8.0 (API 26) or higher
- No special permissions beyond Storage Access Framework (legacy storage permissions are declared only for older APIs)

## Building

### Prerequisites
- JDK 17+
- Android SDK (API 35 recommended)
- Android Studio Ladybug / Koala or newer (or command-line tools)

### Local build

```bash
./gradlew assembleDebug          # Debug APK
./gradlew assembleRelease        # Release APK (minified + resource shrinking)
```

Both debug and release APKs are **signed with the debug keystore** (as requested for this project).

Release build enables:
- `isMinifyEnabled = true`
- `isShrinkResources = true`
- R8 / ProGuard with optimization

APKs appear under:
- `app/build/outputs/apk/debug/`
- `app/build/outputs/apk/release/`

### CI / GitHub Actions

The repository includes a workflow (`.github/workflows/build.yml`) that:

1. Runs on every push / PR to `main`/`master` and on manual dispatch
2. Builds **both** Debug and Release APKs
3. Uploads them as artifacts (`CodeCanvas-debug` and `CodeCanvas-release`)

You can download the APKs from the Actions tab after a successful run.

## Project Structure

```
CodeCanvas/
├── app/
│   ├── src/main/
│   │   ├── java/com/endroid/code/
│   │   │   ├── MainActivity.kt
│   │   │   ├── ThemeMode.kt
│   │   │   ├── editor/
│   │   │   │   ├── Language.kt
│   │   │   │   └── SyntaxHighlighter.kt
│   │   │   ├── ui/
│   │   │   │   ├── CodeCanvasApp.kt
│   │   │   │   ├── components/EditorScreen.kt
│   │   │   │   └── theme/
│   │   │   └── viewmodel/EditorViewModel.kt
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── .github/workflows/build.yml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/
└── README.md
```

## Architecture

- **Single Activity** + Compose
- **MVVM** with `EditorViewModel` holding UI state via `StateFlow`
- **Syntax highlighting** performed on the main thread with a lightweight custom tokenizer (fast enough for typical mobile file sizes)
- File I/O is done on `Dispatchers.IO`
- Persistable URI permissions are taken so files remain accessible across sessions

## Known Limitations / Future Ideas

- Syntax highlighting is regex / token based (not a full LSP)
- No multi-tab / multi-file project view yet (single buffer)
- Search is visual only (highlighting of matches can be added)
- Very large files (> few MB) may feel slower due to full re-highlight

## License

MIT License – feel free to use, modify, and distribute.

---

Made with ❤️ using Kotlin & Jetpack Compose.
