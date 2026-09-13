# CodeCanvas

**A sleek, accessible, lightweight code editor for Android** built with **Kotlin** and **Jetpack Compose (Material 3 / Material You)**.

![Min API 26](https://img.shields.io/badge/minSdk-26-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-purple)
![Version](https://img.shields.io/badge/version-2.0.1-blue)

## What’s new in 2.0.0

- **Material You**: full M3 surface containers, optional wallpaper dynamic color
- **Faster loading**: skip highlight for plain text / large files; cached `derivedStateOf` highlighting
- **Polished editor UI**: surface-container app bar, snackbars, elevated loading card, denser status bar
- **48dp touch targets** on toolbar and search close
- **New adaptive icon** (code canvas + `</>` mark)
- **Smaller APK**: optimized resource shrinking, `resourceConfigurations`, tighter ProGuard / packaging
- Word wrap **off by default**; dedicated Settings with dropdowns & switches

## Features

- Syntax highlighting (Kotlin, Java, Python, JS/TS, XML, JSON, Markdown, SQL, CSS, Shell, C/C++, Rust, Go…)
- Line numbers & word wrap (Settings)
- Undo / Redo · Search · Open / Save / Save As (SAF)
- Open from other apps
- Theme: System / Light / Dark / **Material You**
- Adjustable font size · Language auto-detect + override
- Accessible (content descriptions, semantics, contrast)

## Build

```bash
./gradlew assembleDebug
./gradlew assembleRelease   # minify + shrinkResources, debug-signed
```

CI (GitHub Actions) builds both APKs on every push and uploads them as artifacts.

## Size & performance notes

- R8 full mode + `isShrinkResources` + `android.r8.optimizedResourceShrinking`
- English + xxhdpi resource configurations only
- Core Material icons only (no extended set)
- Highlighting capped at ~80k characters; plain text skips tokenization

## Requirements

- Android 8.0 (API 26)+

## License

MIT
