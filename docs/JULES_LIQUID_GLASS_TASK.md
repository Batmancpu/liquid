# Jules task — finish the Liquid Glass laboratory

This repository is isolated. Do not modify brrrBoard.

Package/application ID:
mangoloads.liquid.com

APK filename:
mangoloads.liquid.com.apk

## Goal

Produce a polished experimental Android APK proving two things:
1. high-quality Liquid Glass optics;
2. real live ambient color/haze from content behind an IME window.

The repository already contains:
- an optical Activity using the QWEA0 View implementation;
- an InputMethodService using Android native cross-window background blur;
- research/attribution files;
- an automated pre-release workflow.

## Phase A — optical quality

Keep QWEA0 as the baseline unless a measured alternative is clearly better.

Research and benchmark:
- QWEA0/Liquid-Glass-Android
- Kyant0/AndroidLiquidGlass
- lvlrSajjad/react-native-blur-overlay

Do not reinvent the refraction mathematics.

Validate:
- center comparatively clear;
- strongest distortion near rounded perimeter/bevel;
- edge compression/magnification on high-detail backgrounds;
- tiny chromatic dispersion near strong curvature;
- thin Fresnel/rim response;
- normal-driven specular highlight;
- subtle touch deformation;
- no medial-axis seam on short pills.

If QWEA0 is heavier than necessary, preserve the same optical model but simplify only the rendering pipeline.

## Phase B — ambient IME haze

The IME must use public Android native window background blur:
Window.setBackgroundBlurRadius
WindowManager.isCrossWindowBlurEnabled
and cross-window blur state monitoring where useful.

Do not use MediaProjection.
Do not use screenshot-per-frame loops.
Do not use bitmap capture loops.
Do not request broad storage or overlay permissions.

Desired effect:
blue content immediately above the keyboard -> soft blue haze in the upper glass
red content -> soft red haze
bright content -> brighter glass
dark content -> darker glass

The effect must be environmental and live, not a fixed theme tint.

Test:
- blue/red/green text fields;
- scrolling content;
- images;
- video where the OEM permits it.

## Phase C — combine

Preferred architecture:
real app content
-> native cross-window blur / ambient backdrop
-> translucent IME glass
-> procedural/AGSL optical treatment
-> foreground content

Do not claim true cross-window refraction unless live pixels from the other application are actually available to the optical shader.

If Android only exposes the compositor-blurred result across the window boundary, keep native ambient haze as the real environmental component and keep optical refraction in the in-app optical lab.

Do not add screenshot capture just to force cross-window refraction.

## Device
Primary target:
OPPO Reno10 5G
ColorOS 15
Android 15

Runtime-check actual capability. Do not assume generic AOSP support means ColorOS support.

## Performance
Measure idle, normal touch, continuous drag, animation and video.
Record FPS/frame time, jank, CPU, GPU/render workload when exposed, memory, temperature and battery/current where exposed.
Idle must not run an unnecessary continuous animation loop.

## Release gate
- APK builds;
- APK installs;
- launcher opens;
- optical scene renders;
- Liquid Lab appears as an IME;
- IME can be selected;
- native cross-window blur capability is explicitly reported;
- basic IME path does not crash.

Keep the repository focused on the laboratory.
