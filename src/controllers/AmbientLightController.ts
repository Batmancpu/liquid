import { AmbientState } from '../types';

export class AmbientLightController {
  private state: AmbientState = {
    lux: 20,
    intensity: 0.32,
    lightX: 0,
    lightY: -0.55,
  };

  private listeners: Set<(state: AmbientState) => void> = new Set();
  private running: boolean = false;
  private hasDeviceMotion: boolean = false;

  constructor() {
    this.updateIntensity(this.state.lux);
  }

  public getState(): AmbientState {
    return { ...this.state };
  }

  public subscribe(listener: (state: AmbientState) => void): () => void {
    this.listeners.add(listener);
    listener(this.getState());
    return () => this.listeners.delete(listener);
  }

  private notify(): void {
    const currentState = this.getState();
    this.listeners.forEach((l) => l(currentState));
  }

  public start(): void {
    if (this.running) return;
    this.running = true;

    if (typeof window !== 'undefined' && 'DeviceOrientationEvent' in window) {
      window.addEventListener('deviceorientation', this.handleOrientation, { passive: true });
    }
  }

  public stop(): void {
    if (!this.running) return;
    this.running = false;

    if (typeof window !== 'undefined') {
      window.removeEventListener('deviceorientation', this.handleOrientation);
    }
  }

  private handleOrientation = (event: DeviceOrientationEvent): void => {
    // gamma is left/right (-90 to 90)
    // beta is front/back tilt (-180 to 180)
    if (event.gamma === null || event.beta === null) return;
    this.hasDeviceMotion = true;

    // Normalize roughly into -1..1 range equivalent to -gx/9.8, -gy/9.8
    const normX = Math.max(-1, Math.min(1, (event.gamma || 0) / 45));
    const normY = Math.max(-1, Math.min(1, ((event.beta || 0) - 45) / 45));

    this.state = {
      ...this.state,
      lightX: normX,
      lightY: normY,
    };
    this.notify();
  };

  public setLux(lux: number): void {
    const clampedLux = Math.max(0, lux);
    // Log-shaped response matching Android AmbientLightController:
    // intensity = ln(1 + lux) / ln(1 + 500)
    const intensity = Math.max(
      0,
      Math.min(1, Math.log(1 + clampedLux) / Math.log(1 + 500))
    );

    this.state = {
      ...this.state,
      lux: clampedLux,
      intensity,
    };
    this.notify();
  }

  public setLightDirection(x: number, y: number): void {
    const clampedX = Math.max(-1, Math.min(1, x));
    const clampedY = Math.max(-1, Math.min(1, y));

    this.state = {
      ...this.state,
      lightX: clampedX,
      lightY: clampedY,
    };
    this.notify();
  }

  private updateIntensity(lux: number): void {
    const intensity = Math.max(
      0,
      Math.min(1, Math.log(1 + lux) / Math.log(1 + 500))
    );
    this.state.intensity = intensity;
  }

  public hasRealMotion(): boolean {
    return this.hasDeviceMotion;
  }
}

export const globalAmbientLight = new AmbientLightController();
