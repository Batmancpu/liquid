export type KeyAction = 'TEXT' | 'BACKSPACE' | 'ENTER' | 'SHIFT' | 'SPACE' | 'MODE';

export interface KeyDef {
  label: string;
  action: KeyAction;
  value?: string;
  weight?: number;
}

export interface AmbientState {
  lux: number;
  intensity: number;
  lightX: number;
  lightY: number;
}

export type BackdropMode = 'orbs' | 'blue' | 'red' | 'green' | 'dark' | 'grid' | 'photo' | 'scrolling';

export type OpticalLensShape = 'keyboard-card' | 'pill' | 'circle' | 'dock-bar' | 'squircle';

export interface OpticalParameters {
  ior: number; // Index of refraction (1.1 to 1.8)
  bevelRadius: number; // Bevel curve width in px (10 to 60)
  chromaticDispersion: number; // RGB offset strength (0.0 to 1.0)
  specularPower: number; // Highlight sharpness (8 to 40)
  rimIntensity: number; // Rim Fresnel response (0.0 to 2.0)
  magnification: number; // Center scale factor (0.9 to 1.4)
  blurRadius: number; // Cross-window compositor blur radius (e.g. 78px in Android)
}

export interface DiagnosticsData {
  fps: number;
  frameTimeMs: number;
  isCrossWindowBlurSupported: boolean;
  isBatterySaver: boolean;
  actualBlurRadius: number;
  cpuLoadEstimate: string;
  targetDevice: string;
}
