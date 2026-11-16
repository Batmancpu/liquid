# Liquid — mandatory agent instructions

This repository is the standalone `Batmancpu/liquid` experimental Android keyboard and React optics laboratory. **Do not modify `Batmancpu/brrrBoard` or import GPL FrostKeys source.** The owner's target is a real `InputMethodService` that can show environmental Liquid Glass over **other apps**, not merely a demo rendered inside Liquid's Activity.

## Resume every session

1. Read [`context.md`](context.md) first. It is the short, current checkpoint.
2. Read [`docs/SESSION_LEDGER.md`](docs/SESSION_LEDGER.md), [`docs/DECISIONS.md`](docs/DECISIONS.md), [`docs/ROADMAP.md`](docs/ROADMAP.md), [`docs/JULES_LIQUID_GLASS_TASK.md`](docs/JULES_LIQUID_GLASS_TASK.md), and [`docs/THIRD_PARTY.md`](docs/THIRD_PARTY.md).
3. Check `git status --short`, `git log -5 --oneline`, current `main` head, and the latest GitHub Actions run. Git history and tested runtime evidence supersede old completion claims.
4. Continue from the **exact next action** recorded in `context.md`. Do not assume an APK was built, installed, or optically verified unless the evidence says so.

## Mandatory continuously updated handoff

- Work in small, reviewable commits on **`main` only**. Do not create development branches or merge unrelated projects.
- **Before each code batch**, add a dated entry in the append-only `docs/SESSION_LEDGER.md` stating the original owner request, start SHA, goal, files intended, design choice, rejection reasons, and acceptance tests. Refresh `context.md` when the active objective changes.
- **After each batch**, append exactly what happened: every changed file and meaningful symbol, why, expected and observed behavior, commands run, passing/failing/skipped tests, commit subject/base SHA, workflow run and artifact, remaining issue, and exact next action. Keep `context.md` pointed at the actual latest state. A commit cannot know its own SHA; record the base SHA and intended subject in that commit, then reference the resulting SHA in a later checkpoint.
- Preserve failed attempts, negative evidence and revisions. Distinguish **implemented**, **compiled/CI-tested**, **verified on OPPO Reno10 5G**, **owner-reported**, and **hypothesis**. Do not promote an implementation claim to on-device success.
- Document notable research with direct source URLs and dates, device limitations, licensing choices, and rejected approaches in `docs/DECISIONS.md`. Keep the planned-vs-done status in `docs/ROADMAP.md`.
- Reconcile ledger with `git diff --check`, status and changed paths before each commit. Do not duplicate silently or invent evidence.
- The repository is public: never log secrets, credentials, signing keys, private typed text, clipboard contents, personal screenshots or raw device logs.

## Product constraints

- Cross-app native IME is the product; React/Vite optics simulator is a separate research bench and must not be mistaken for a functional Android IME.
- Use Android public cross-window window blur and observable capability checks on API 31+, with a visually coherent fallback if the OEM disables the compositor effect. Glass rims/optical highlights may be custom drawn on the keyboard's own surfaces; do not claim raw behind-app pixel refraction without an authorized pixel source.
- No MediaProjection, screenshot loops, root assumptions, broad overlay/storage permission, analytics, network/AI keyboard APIs, or invasive capture of typed content.
- Prioritize stable typing, secure-field behavior and legibility, especially on OPPO Reno10 5G / ColorOS 15 / Android 15. Measure effects on the real device before calling them proven.
- Keep the current application ID stable during the first proof-of-concept unless an explicitly documented migration is required. The older task document names `mangoloads.liquid.com`, whereas current Gradle sets `com.mangoloads.expmango`; do not conceal the discrepancy.
- Build the APK through CI, confirm APK metadata and provide a directly installable link when successful. A green CI build is **not** a real-device blur or install test.
