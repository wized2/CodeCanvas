## CodeCanvas 3.0.0

Fast Material 3 code editor — **typing no longer freezes the UI**.

### Performance
- Syntax highlight is **debounced** (~220ms after you stop typing)
- While typing: single visible text layer (no dual underlay paint)
- Highlight budget capped at **40k** characters; larger files stay editable
- Line gutter capped to avoid huge string builds

### Still included
- Open / Save / Save As (SAF), undo/redo, search, themes, line numbers, word wrap
- Open from other apps · Material 3

**CodeCanvas-release.apk** · Min SDK 26 · MIT · offline · no ads
