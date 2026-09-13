# CodeCanvas

**A sleek, accessible, lightweight code editor for Android** built with **Kotlin** and **Jetpack Compose**.

![Min API 26](https://img.shields.io/badge/minSdk-26-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-purple)
![Version](https://img.shields.io/badge/version-1.1.0-blue)

## What’s new in 1.1.0

- **Dedicated Settings screen** with proper switches, dropdowns and slider (no more cluttered overflow menu)
- **Much better performance**: faster file open, no freezes when changing settings, efficient line numbers, capped highlighting for large files
- **Smaller APK**: removed heavy `material-icons-extended` and navigation dependency
- **Fixed status-bar gap** under the top bar
- Cleaner, more polished UI

## Features

- Syntax highlighting (Kotlin, Java, Python, JS/TS, XML, JSON, Markdown, SQL, CSS, Shell, C/C++, Rust, Go…)
- Line numbers & word wrap (toggleable in Settings)
- Undo / Redo
- Search
- Open / Save / Save As (Storage Access Framework)
- Open from other apps
- Theme: System / Light / Dark (Tokyo Night inspired)
- Adjustable font size
- Language auto-detect + override
- Accessible (content descriptions, semantics, contrast)

## Build

```bash
./gradlew assembleDebug
./gradlew assembleRelease   # minify + shrinkResources, debug-signed
```

CI (GitHub Actions) builds both APKs on every push and uploads them as artifacts.

## Requirements

- Android 8.0 (API 26)+

## License

MIT
