# Contributing to CodeCanvas

## Build
```bash
./gradlew assembleRelease
```
JDK 17. Release signing uses the same `RELEASE_*` secrets pattern as Fern.

## Scope
- Keep the editor **fast**: debounce highlighting, cap annotated length
- Prefer Material 3 patterns already in the app
- Update `RELEASE_NOTES.md` for user-visible changes

## PRs
Small focused diffs. CI must stay green.
