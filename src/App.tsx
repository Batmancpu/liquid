import React, { useState, useEffect } from 'react';
import { MobileDeviceFrame } from './components/MobileDeviceFrame';
import { OpticalBench } from './components/OpticalBench';
import { AmbientLightingRig } from './components/AmbientLightingRig';
import { DiagnosticsOverlay } from './components/DiagnosticsOverlay';
import { SettingsModal } from './components/SettingsModal';
import { globalAmbientLight } from './controllers/AmbientLightController';
import { AmbientState, OpticalParameters, BackdropMode } from './types';
import { Smartphone, Eye, Sun, Settings, Sparkles, Layers, Sliders, Shield } from 'lucide-react';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'device' | 'optics' | 'ambient'>('device');
  const [ambientState, setAmbientState] = useState<AmbientState>(globalAmbientLight.getState());
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [isBatterySaver, setIsBatterySaver] = useState(false);
  const [backdropMode, setBackdropMode] = useState<BackdropMode>('orbs');

  const [opticalParams, setOpticalParams] = useState<OpticalParameters>({
    ior: 1.45,
    bevelRadius: 38,
    chromaticDispersion: 0.35,
    specularPower: 22.0,
    rimIntensity: 1.0,
    magnification: 1.05,
    blurRadius: 78, // Matching Android nativeBlurRadius = 78
  });

  useEffect(() => {
    globalAmbientLight.start();
    const unsubscribe = globalAmbientLight.subscribe((state) => {
      setAmbientState(state);
    });

    return () => {
      unsubscribe();
      globalAmbientLight.stop();
    };
  }, []);

  const handleUpdateParams = (updates: Partial<OpticalParameters>) => {
    setOpticalParams((prev) => ({ ...prev, ...updates }));
  };

  const handleUpdateLux = (lux: number) => {
    globalAmbientLight.setLux(lux);
  };

  const handleUpdateDirection = (x: number, y: number) => {
    globalAmbientLight.setLightDirection(x, y);
  };

  return (
    <div className="min-h-screen bg-[#060810] text-slate-100 flex flex-col justify-between">
      {/* Top Navigation & Status Bar */}
      <header className="border-b border-slate-800/80 bg-slate-950/70 backdrop-blur-md sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 h-16 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-blue-600 via-cyan-500 to-emerald-400 p-[1px] flex items-center justify-center shadow-lg shadow-cyan-500/20">
              <div className="w-full h-full bg-[#060810] rounded-[11px] flex items-center justify-center text-lg">
                💧
              </div>
            </div>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="font-bold text-base tracking-tight text-white">Liquid Lab</h1>
                <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-cyan-500/10 text-cyan-300 border border-cyan-500/30">
                  v0.9-ambient-haze
                </span>
              </div>
              <p className="text-[11px] text-slate-400 font-mono hidden sm:block">
                mangoloads.liquid.com • ColorOS 15 / Android 15
              </p>
            </div>
          </div>

          {/* Tab Navigation */}
          <nav className="flex items-center gap-1 bg-slate-900/80 p-1 rounded-xl border border-slate-800">
            <button
              onClick={() => setActiveTab('device')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
                activeTab === 'device'
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
              }`}
            >
              <Smartphone className="w-3.5 h-3.5" />
              <span>Mobile IME</span>
            </button>

            <button
              onClick={() => setActiveTab('optics')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
                activeTab === 'optics'
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
              }`}
            >
              <Layers className="w-3.5 h-3.5" />
              <span>Optical Bench</span>
            </button>

            <button
              onClick={() => setActiveTab('ambient')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
                activeTab === 'ambient'
                  ? 'bg-blue-600 text-white shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
              }`}
            >
              <Sun className="w-3.5 h-3.5" />
              <span>Ambient Rig</span>
            </button>
          </nav>

          {/* Action Settings */}
          <div className="flex items-center gap-2">
            <button
              onClick={() => setIsSettingsOpen(true)}
              className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white hover:border-slate-700 transition-colors"
              title="Open Settings"
            >
              <Settings className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      {/* Main Laboratory Viewport */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-4 sm:p-6 flex flex-col gap-6">
        {activeTab === 'device' && (
          <div className="flex flex-col lg:flex-row items-center justify-center gap-8 py-4">
            {/* Phone Viewport */}
            <div className="flex justify-center w-full lg:w-auto">
              <MobileDeviceFrame
                ambientState={ambientState}
                opticalParams={opticalParams}
                backdropMode={backdropMode}
                isNativeBlurEnabled={!isBatterySaver}
                onOpenSettings={() => setIsSettingsOpen(true)}
                onToggleBatterySaver={() => setIsBatterySaver(!isBatterySaver)}
                isBatterySaver={isBatterySaver}
              />
            </div>

            {/* Side Controls & Quick Toggles */}
            <div className="w-full max-w-md flex flex-col gap-4">
              <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 shadow-xl">
                <div className="flex items-center gap-2 mb-3 pb-2 border-b border-slate-800">
                  <Sparkles className="w-4 h-4 text-cyan-400" />
                  <h3 className="text-sm font-semibold text-white">Environmental Backdrop</h3>
                </div>
                <p className="text-xs text-slate-400 mb-3">
                  Switch the content living directly behind the IME to observe native cross-window ambient haze:
                </p>
                <div className="grid grid-cols-2 gap-2">
                  {[
                    { mode: 'orbs' as const, label: 'Moving Orbs (Default)' },
                    { mode: 'blue' as const, label: 'Cobalt Blue (#1457FF)' },
                    { mode: 'red' as const, label: 'Crimson Red (#FF2D55)' },
                    { mode: 'green' as const, label: 'Vivid Green (#00C853)' },
                    { mode: 'scrolling' as const, label: 'Scrollable List' },
                    { mode: 'photo' as const, label: 'Photo Contrast' },
                  ].map((item) => (
                    <button
                      key={item.mode}
                      onClick={() => setBackdropMode(item.mode)}
                      className={`p-2.5 text-xs rounded-xl font-medium text-left border transition-all ${
                        backdropMode === item.mode
                          ? 'bg-blue-600/20 border-blue-500/80 text-white'
                          : 'bg-slate-950/60 border-slate-800 text-slate-400 hover:text-white'
                      }`}
                    >
                      {item.label}
                    </button>
                  ))}
                </div>
              </div>

              {/* Quick Lighting Controls */}
              <AmbientLightingRig
                ambientState={ambientState}
                onUpdateLux={handleUpdateLux}
                onUpdateDirection={handleUpdateDirection}
              />
            </div>
          </div>
        )}

        {activeTab === 'optics' && (
          <OpticalBench
            ambientState={ambientState}
            params={opticalParams}
            onUpdateParams={handleUpdateParams}
            backdropMode={backdropMode}
            onSelectBackdropMode={setBackdropMode}
          />
        )}

        {activeTab === 'ambient' && (
          <div className="max-w-3xl mx-auto w-full flex flex-col gap-6 py-4">
            <AmbientLightingRig
              ambientState={ambientState}
              onUpdateLux={handleUpdateLux}
              onUpdateDirection={handleUpdateDirection}
            />

            <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 shadow-xl text-xs space-y-3">
              <h4 className="text-sm font-semibold text-white">Physical Illumination Sensor Architecture</h4>
              <p className="text-slate-300 leading-relaxed">
                As detailed in <code className="text-cyan-300 font-mono">AmbientLightController.kt</code>,
                the application intentionally enforces a zero-screen-capture policy:
              </p>
              <ul className="list-disc pl-5 space-y-1.5 text-slate-400">
                <li>
                  <strong className="text-slate-200">Sensor.TYPE_LIGHT:</strong> Provides physical ambient light in lux. The intensity function follows a logarithmic curve:
                  <code className="text-cyan-300 block font-mono mt-0.5">intensity = ln(1 + lux) / ln(1 + 500)</code>
                </li>
                <li>
                  <strong className="text-slate-200">Sensor.TYPE_GRAVITY:</strong> Supplies 3D device tilt angles to drive the physical specular specular highlight across the glass surface.
                </li>
                <li>
                  <strong className="text-slate-200">Zero MediaProjection / Zero Screenshots:</strong> The environmental haze comes exclusively from native OS window composition blur.
                </li>
              </ul>
            </div>
          </div>
        )}

        {/* Global Diagnostics Bar */}
        <DiagnosticsOverlay
          isBatterySaver={isBatterySaver}
          onToggleBatterySaver={() => setIsBatterySaver(!isBatterySaver)}
          actualBlurRadius={opticalParams.blurRadius}
        />
      </main>

      {/* Footer */}
      <footer className="border-t border-slate-800/80 bg-slate-950/80 py-4 px-6 text-center text-xs text-slate-500 font-mono">
        Liquid Lab • Android Liquid Glass Laboratory Ported to React • MIT / Apache-2.0
      </footer>

      {/* Settings Modal */}
      <SettingsModal
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        params={opticalParams}
        onUpdateParams={handleUpdateParams}
        isBatterySaver={isBatterySaver}
        onToggleBatterySaver={() => setIsBatterySaver(!isBatterySaver)}
      />
    </div>
  );
};
