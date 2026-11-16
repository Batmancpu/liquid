# Liquid — implementation and acceptance roadmap

Updated 16 November 2026 IST. Entries are **planned** unless checked and evidence is linked in the ledger.

## Phase 0 — reliability and memory

- [x] Inspect main branch, Android and React architecture, previous CI failure and license notes.
- [x] Research cross-window blur versus shader access and Apple's materials guidance.
- [x] Establish and commit `AGENTS.md`, `context.md`, chronological ledger and decision log (`d84bd36`).

## Phase 1 — first native cross-app Liquid test APK

- [ ] Fix missing `android.useAndroidX` build configuration and compile current baseline.
- [ ] Set a truly translucent IME window and paint an ambient-blur-compatible chassis.
- [ ] Enable Android cross-window background blur with runtime capability check/listener and visible fallback.
- [ ] Draw usable, rounded keys with subtle rim highlights, key press response, high-contrast light/dark labels.
- [ ] Preserve functioning typing, delete, enter, shift, numeric mode and secure-field policies.
- [ ] Run offline/privacy audit, clean debug Android build and verify artifact path/package metadata.
- [ ] Offer installable APK and record exact run, source SHA and checksums when available.

## Phase 2 — device proof (owner on OPPO Reno10 5G, ColorOS 15)

- [ ] Enable/select Liquid as Android keyboard and type in **at least three unrelated apps**; don't accept only in-app demo.
- [ ] Test backgrounds with bright blue, red, green, grey, black, moving/scrolling imagery; record whether a real live haze responds.
- [ ] Test power saver, blur-disabled fallback, light/dark system theme, rotation, keyboard dismiss and resize; check transparency flicker.
- [ ] Confirm no input lag, excessive heat or idle rendering, crash or privacy issue. Record phone results as owner-reported.

## Phase 3 — optical polish after device gate

- [ ] Use evidence to tune blur radius, alpha and per-key rim response for legibility.
- [ ] Benchmark QWEA0 native optics against lightweight shaders. Keep any external library licenses and avoid incompatible GPL source.
- [ ] Tune touch affordance, suggestion strip, bottom row, switcher and accessibility to Apple-inspired proportions.
- [ ] Decide production naming/package/signing separately. No production merge into brrrBoard without an explicit user decision.
