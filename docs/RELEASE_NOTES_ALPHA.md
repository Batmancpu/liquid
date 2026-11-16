# Liquid Glass Lab — first Android cross-app test (experimental)

**Debug-signed proof of concept, not a finished keyboard.** It has passed Android GitHub Actions compilation but is not yet installed or visually verified on the owner's OPPO Reno10 5G (Android 15 / ColorOS 15).

## Implemented

- Native Android InputMethodService usable as a system keyboard in unrelated apps.
- Android 12+ native cross-window background blur request, translucent rounded keycaps, rim highlights and a fallback when system blur is reported disabled.
- QWERTY and number modes, basic prediction, offline local learning, English/Hinglish support and light/dark system appearance.
- No INTERNET, screen capture, MediaProjection or cloud/telemetry permissions.

## Important limits

- **The live cross-app haze is not yet verified.** Android says background blur requires a translucent floating window; normal IME windows may not be floating. ColorOS or Battery Saver may disable it. This APK is a controlled experiment to establish what really works on the phone, not proof of Apple-level refraction.
- Other advanced keyboard functions, iOS-exact spacing, swipe typing, full emoji/clipboard and measured GPU performance are not ready.
- This APK uses **temporary Android debug signing**. A later APK produced on a fresh CI runner may have a different signing certificate, requiring you to uninstall this preview before installing that next build. Do not treat this as a permanent Obtainium update base.

## Phone test

1. Install `LiquidGlassLab-test.apk`, open **Liquid Glass Lab**, enable it in Android keyboard settings, then switch to it.
2. Open another app (browser, notes or messaging) and test typing, backspace, numeric mode and Return.
3. Compare the keyboard while the other app displays blue, red, bright and dark areas; try scrolling, and then Battery Saver.
4. Report whether colors/haze actually update behind the keys. Do not include private typed content in screenshots or videos.

Project resume: `context.md`, `docs/SESSION_LEDGER.md`, `docs/DECISIONS.md`.
