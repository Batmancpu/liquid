# Liquid Lab (React Edition)

Experimental Liquid Glass optics, procedural ambient illumination, and cross-window ambient haze laboratory rewritten in React and TypeScript.

Originally imported from `Batmancpu/liquid` (Android / ColorOS 15 IME laboratory).

## Features Ported & Architecture

1. **Phase 1: Liquid Glass Optics (QWEA0 / Kyant0 Model)**
   - Clear interior with continuous center falloff.
   - Strong edge curvature distortion driven by signed distance field gradients (`sdRoundRect`).
   - Chromatic dispersion (wavelength-dependent RGB offset near steep curvature).
   - Fresnel rim response (`rim = edge^1.55`).
   - Normal-driven specular highlights reacting dynamically to physical light vectors.
   - Verified: No medial-axis seam on short pills or squircle shapes.
   - Interactive Optical Bench with customizable shapes (Short Pill, Keyboard Card, Circular Lens, Dock Bar, Squircle).

2. **Phase 2: Ambient IME & Cross-Window Haze**
   - ColorOS 15 / Android 15 reference container (OPPO Reno10 5G viewport).
   - Multi-layered IME glass panel:
     - Outer 38px beveled glass chassis.
     - Individual glass key caps with top-edge specular line highlights and press states.
     - Full QWERTY layout with Shift, Backspace, Space, Enter, and numeric mode switching.
     - Active text editing and color haze testing (Cobalt Blue #1457FF, Crimson Red #FF2D55, Vivid Green #00C853, Obsidian Dark, and scrollable feeds).
   - Zero-screen-capture policy: Uses native compositor backdrop blur simulation (`backdrop-filter: blur(78px)`).
   - Battery Saver auto-dim toggle (turns off blur to conserve GPU performance, matching Android `isBatterySaver()`).

3. **Procedural Physical Ambient Illumination**
   - Virtual sensor controller modeling `Sensor.TYPE_LIGHT` and `Sensor.TYPE_GRAVITY`.
   - Logarithmic illuminance response: `intensity = ln(1 + lux) / ln(1 + 500)`.
   - Real DeviceOrientation support when run on physical mobile browsers, paired with interactive 2D tilt pad and lux presets.

4. **Performance & Diagnostics Telemetry**
   - Real-time FPS and frame time monitoring.
   - Compositor blur capability detection (`CSS.supports('backdrop-filter')`).
   - Zero jank verification under continuous drag and animation.

## Development

- Start local dev server: `npm run dev` (runs Vite on `0.0.0.0:3000`)
- Build for production: `npm run build`
