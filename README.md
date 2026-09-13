# CodeCanvas

**A sleek, accessible, lightweight code editor for Android** built with **Kotlin** and **Jetpack Compose**.

![Min API 26](https://img.shields.io/badge/minSdk-26-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-purple)
![Version](https://img.shields.io/badge/version-1.2.0-blue)

## What’s new in 1.2.0

- Faster file open (streaming + 1 MB soft limit) with full-screen loading overlay + message
- Word wrap **off by default** (enable in Settings)
- Larger 48dp touch targets on toolbar buttons
- Improved text area padding and status bar
- Dedicated Settings screen (switches, dropdowns, slider)
- Smaller APK (core icons only, tighter ProGuard / packaging)
- No freezes when changing settings; efficient line numbers & capped highlighting

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
