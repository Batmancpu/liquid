# Liquid — research, decisions and rejected approaches

Updated 16 November 2026 IST. Distinguish source-backed platform capability from device verification.

## D-001 — The experiment must operate outside its own app

- **Chosen:** Android `InputMethodService` rendered in the system IME window, so it can be selected when typing in unrelated apps. Android's cross-window background blur is the only planned source of **live ambient** variation from behind that IME.
- **Rejected:** WebView/React-only mock keyboard. Its CSS `backdrop-filter` operates within a web scene and does not prove cross-app behavior.
- **Rejected:** MediaProjection or screenshots to obtain arbitrary pixels beneath the keyboard; intrusive, permission-heavy and contrary to the stated privacy goal.
- **Technical boundary:** Native compositor blur can blend the underlying window's appearance, but `RuntimeShader` does not automatically sample other applications. Edge refraction applied only to *our own* content must not be labelled true cross-window refraction.
- References: https://source.android.com/docs/core/display/window-blurs ; https://developer.android.com/reference/android/view/Window ; https://developer.android.com/develop/ui/views/graphics/agsl/using-agsl .

## D-002 — Capability detection and fallback

- **Chosen:** API 31+ `WindowManager.isCrossWindowBlurEnabled`, register a live listener, and enable window background blur only when supported. Keep translucent dark/light glass layers, rims and readable lettering, reverting to an opaque enough static surface when native blur is off.
- **Reason:** AOSP notes OEM availability and runtime disablement (battery saver and other conditions). The OPPO Reno10 5G ColorOS 15 behavior must be tested on the phone.
- **Rejected:** Unconditional blur strength claims and forced continual redraws; neither proves visible behind-app haze.
- Reference: https://source.android.com/docs/core/display/window-blurs .

## D-003 — Visual language

- **Chosen:** Apple-inspired rounded keys, consistent spacing, translucent chassis, subtle bright perimeter/specular highlights, high-contrast labels, restrained action tint, and support for dark/light mode. Animations should respond quickly to touch without a constant idle renderer.
- **Rejected:** Maximal refraction on each key regardless of readability, or broad whole-panel saturated tint which obscures ambient content.
- Reference: https://developer.apple.com/design/human-interface-guidelines/materials ; https://developer.apple.com/design/human-interface-guidelines/virtual-keyboards .
- **Important:** Apple does not grant access to its private iOS keyboard rendering implementation; we cannot guarantee pixel-for-pixel matching.

## D-004 — Optical implementation sequence

- **Chosen now:** make the real cross-app native blur path work before a costly shader integration. Draw lightweight static glass-edge/specular cues directly in the native View.
- **Evaluate later:** QWEA0/Liquid-Glass-Android for more physically grounded optics, or Kyant0/AndroidLiquidGlass when a supported runtime capture/backdrop source exists; benchmark on-device before committing to either dependency.
- **Rejected now:** assuming the web optical simulator or a library's in-app preview automatically unlocks cross-window pixel access.
- References: https://github.com/QWEA0/Liquid-Glass-Android (MIT); https://github.com/Kyant0/AndroidLiquidGlass (Apache-2.0); `docs/THIRD_PARTY.md` (currently inaccurate about an already pinned Android dependency).

## D-005 — Compatibility, privacy and release

- **Chosen:** leave current Android application ID `com.mangoloads.expmango` unchanged for the first build, explicitly document its conflict with older Jules task's desired `mangoloads.liquid.com`. Keep it offline and avoid copy-pasting GPL FrostKeys code. Build via Actions, check artifact and test on OPPO before any production claim.
- **Rejected:** changing signing/package identity mid-proof, fabricating release success, or modifying the production brrrBoard codebase.
