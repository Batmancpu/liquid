# Liquid — live continuation checkpoint

Updated 16 November 2026 (IST). Read `AGENTS.md` before modifying anything; full chronology in `docs/SESSION_LEDGER.md`.

## Goal and boundaries

Deliver a first working, installable Android **Liquid Glass IME** from `Batmancpu/liquid`: keyboard works in unrelated apps; Android compositor can reveal live, app-dependent background haze where the device permits it; clean glass keycaps and readable layout; robust opaque/translucent fallback. Use public Android APIs and no screen capture. Keep `brrrBoard` untouched.

## Source of truth at start of this session

- Target repo: `https://github.com/Batmancpu/liquid`; branch `main`; audited starting SHA `6417dffb89a7a05dcbfe6155c5f62181f5d51606` (19 October 2026).
- Native Android source lives under `android/`, module `:app` by Gradle mapping. Package/application ID currently `com.mangoloads.expmango`, Android minSdk 26, compileSdk/targetSdk 34.
- React/Vite `src/` models the visual look **inside a web page**, and is not the deployed Android keyboard. README currently describes the React lab and overstates native optical implementation.
- Current native IME (`MangoImeService`, `MangoKeyboardView`) draws an opaque dark rectangle; it does **not** invoke `Window.setBackgroundBlurRadius` or monitor `isCrossWindowBlurEnabled`.
- Latest pre-session CI run `39292839403` failed at `:app:checkDebugAarMetadata`: root `gradle.properties` missing `android.useAndroidX=true`. No successful APK can be inferred from this run.
- `docs/JULES_LIQUID_GLASS_TASK.md` describes the environmental blur goal and QWEA0 optical reference, but QWEA0 is **not currently a Gradle dependency**. `docs/THIRD_PARTY.md` incorrectly presents it as pinned; verify claims before repeating.

## Feasibility and design decision

- Android 12+ cross-window background blur can show underlying app content via the system compositor if supported and enabled. It may be restricted by device settings, battery saving and some media scenes. API documentation: https://source.android.com/docs/core/display/window-blurs and https://developer.android.com/reference/android/view/Window.
- Android's AGSL shader can treat drawing **within the IME app**, but does not thereby obtain arbitrary other apps' pixels: https://developer.android.com/develop/ui/views/graphics/agsl/using-agsl.
- Apple guidance describes Liquid Glass as a distinct functional/control layer, advises sparing use and legibility: https://developer.apple.com/design/human-interface-guidelines/materials. We target a similar feel, not a claim of Apple's private implementation.
- Preferred approach: native cross-window blurred translucent IME chassis + custom key rim/specular drawing + opaque fallback. Avoid MediaProjection, screenshot loops and a WebView keyboard.

## Current batch and next action

**Batch 0 complete:** handoff foundation committed on `main` as `d84bd36e0f655f1d25c8bbc762c8ec678b8928c0`.

**Batch 1 committed:** AndroidX build configuration and handoff as `377de3e16c45abf061e1fdff5d4a48ce5cbb300f`. No successful Actions run for that SHA was yet visible when Batch 2 began; do not call Batch 1 independently compile-tested.

**Batch 2 committed:** `1aaae3c32be3f5e6caf18a4170e5deb69c3bdfb2` on `main`. Native `MangoImeService` sets the actual InputMethodService window transparent, observes cross-window blur enablement and sets `Window.setBackgroundBlurRadius` on Android 12+; `MangoKeyboardView` draws a semi-transparent light/dark glass surface with rim/specular details, measured phone-sized height and opaque fallback; launcher displays capability and cross-app test instructions; app/IME labels say Liquid Glass. Local `scripts/audit-offline.sh` and `git diff --check` passed. **No new CI build has appeared** for `377de3e` or `1aaae3c` after the Git Data API ref updates; only the preceding docs-only failed run is visible. The browser is not signed in to trigger Actions manually. Next: use the connected GitHub Contents API for a legitimate handoff checkpoint, re-check CI trigger, inspect build logs, resolve compiler errors, and offer an APK only after successful assembly. Cross-app behavior remains phone-unverified.

**Build success:** GitHub Actions run `43800132882` on `60bace1` passed Android compile, APK assembly, offline audit and artifact upload. Earlier “no new CI” note is superseded.

**Batch 3:** ship a direct GitHub Releases APK using the gated `release:` Actions path and standard temporary debug signing. The release has to be verified before being called live. Critical known risk: Android docs require the window to be both translucent and floating for `setBackgroundBlurRadius` to show; the system IME may be nonfloating. This is a device feasibility test. Permanent signing needs securely configured GitHub Actions secrets; do not publish a keystore.

## Release state

No verified new APK from this session yet. Do not claim installation, OEM blur support, optical performance or battery performance without evidence.
