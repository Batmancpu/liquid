import React from 'react';
import { AmbientState } from '../types';
import { Sun, Compass, Sparkles, Smartphone, Moon, Zap } from 'lucide-react';

interface AmbientLightingRigProps {
  ambientState: AmbientState;
  onUpdateLux: (lux: number) => void;
  onUpdateDirection: (x: number, y: number) => void;
}

export const AmbientLightingRig: React.FC<AmbientLightingRigProps> = ({
  ambientState,
  onUpdateLux,
  onUpdateDirection,
}) => {
  const luxPresets = [
    { label: 'Dim Room (5 lx)', lux: 5, icon: Moon },
    { label: 'Lab Standard (20 lx)', lux: 20, icon: Smartphone },
    { label: 'Office Desk (100 lx)', lux: 100, icon: Sparkles },
    { label: 'Bright Studio (500 lx)', lux: 500, icon: Sun },
    { label: 'Direct Sunlight (1000 lx)', lux: 1000, icon: Zap },
  ];

  const handlePadPointerDown = (e: React.PointerEvent<HTMLDivElement>) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const updateFromPointer = (clientX: number, clientY: number) => {
      const relX = (clientX - rect.left) / rect.width;
      const relY = (clientY - rect.top) / rect.height;
      // map 0..1 to -1..1
      const x = (relX - 0.5) * 2;
      const y = (relY - 0.5) * 2;
      onUpdateDirection(x, y);
    };

    updateFromPointer(e.clientX, e.clientY);
    (e.target as HTMLElement).setPointerCapture(e.pointerId);

    const onPointerMove = (moveEvent: PointerEvent) => {
      updateFromPointer(moveEvent.clientX, moveEvent.clientY);
    };

    const onPointerUp = () => {
      window.removeEventListener('pointermove', onPointerMove);
      window.removeEventListener('pointerup', onPointerUp);
    };

    window.addEventListener('pointermove', onPointerMove);
    window.addEventListener('pointerup', onPointerUp);
  };

  return (
    <div className="bg-slate-900/80 border border-slate-800 rounded-2xl p-5 shadow-xl">
      <div className="flex items-center justify-between mb-4 pb-2 border-b border-slate-800">
        <div className="flex items-center gap-2">
          <Sun className="w-4 h-4 text-amber-400" />
          <h4 className="text-sm font-semibold text-white">Ambient Physics Controller</h4>
        </div>
        <span className="text-[11px] font-mono text-cyan-400">Sensor.TYPE_LIGHT & TYPE_GRAVITY</span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Lux Intensity Section */}
        <div>
          <div className="flex justify-between items-baseline mb-1">
            <span className="text-xs font-mono text-slate-300">Ambient Illuminance</span>
            <span className="text-sm font-mono font-bold text-amber-400">
              {Math.round(ambientState.lux)} lx
            </span>
          </div>

          <input
            type="range"
            min="0"
            max="1000"
            step="5"
            value={ambientState.lux}
            onChange={(e) => onUpdateLux(parseFloat(e.target.value))}
            className="w-full accent-amber-400 h-2 bg-slate-800 rounded-lg cursor-pointer"
          />

          <div className="mt-2 flex items-center justify-between text-xs text-slate-400 font-mono">
            <span>Calculated Glass Intensity:</span>
            <span className="text-white font-bold">{(ambientState.intensity * 100).toFixed(1)}%</span>
          </div>
          <div className="w-full bg-slate-800 h-1.5 rounded-full mt-1 overflow-hidden">
            <div
              className="bg-gradient-to-r from-amber-500 to-amber-300 h-full transition-all duration-150"
              style={{ width: `${ambientState.intensity * 100}%` }}
            />
          </div>

          <p className="text-[10px] text-slate-500 mt-1 font-mono">
            Model: intensity = ln(1 + lux) / ln(1 + 500)
          </p>

          {/* Quick presets */}
          <div className="flex flex-wrap gap-1.5 mt-3">
            {luxPresets.map((preset) => (
              <button
                key={preset.label}
                onClick={() => onUpdateLux(preset.lux)}
                className={`text-[10px] font-mono px-2 py-1 rounded-md border transition-all ${
                  Math.abs(ambientState.lux - preset.lux) < 5
                    ? 'bg-amber-500/20 text-amber-300 border-amber-500/50'
                    : 'bg-slate-800/60 text-slate-400 border-slate-700 hover:text-white'
                }`}
              >
                {preset.label}
              </button>
            ))}
          </div>
        </div>

        {/* Direction & Gyro Tilt Section */}
        <div>
          <div className="flex justify-between items-baseline mb-1">
            <span className="text-xs font-mono text-slate-300">Device Tilt & Light Direction</span>
            <span className="text-xs font-mono text-cyan-400">
              X:{ambientState.lightX.toFixed(2)} Y:{ambientState.lightY.toFixed(2)}
            </span>
          </div>

          {/* Interactive Tilt Trackpad */}
          <div
            onPointerDown={handlePadPointerDown}
            className="relative w-full h-32 rounded-xl bg-slate-950 border border-slate-800 overflow-hidden cursor-crosshair flex items-center justify-center select-none"
          >
            {/* Center crosshairs */}
            <div className="absolute inset-x-0 top-1/2 h-px bg-slate-800 pointer-events-none" />
            <div className="absolute inset-y-0 left-1/2 w-px bg-slate-800 pointer-events-none" />
            <div className="w-16 h-16 rounded-full border border-slate-800/80 pointer-events-none" />

            {/* Moving light target indicator */}
            <div
              style={{
                left: `${(ambientState.lightX * 0.5 + 0.5) * 100}%`,
                top: `${(ambientState.lightY * 0.5 + 0.5) * 100}%`,
              }}
              className="absolute -translate-x-1/2 -translate-y-1/2 w-6 h-6 rounded-full bg-cyan-400/30 border border-cyan-300 shadow-[0_0_15px_rgba(34,211,238,0.8)] pointer-events-none flex items-center justify-center transition-all duration-75"
            >
              <div className="w-2 h-2 rounded-full bg-cyan-200" />
            </div>

            <div className="absolute bottom-1 right-2 text-[9px] font-mono text-slate-600 pointer-events-none">
              Drag to tilt device
            </div>
          </div>

          <div className="flex gap-2 mt-2">
            <button
              onClick={() => onUpdateDirection(0, -0.55)}
              className="flex-1 text-[10px] font-mono py-1 rounded bg-slate-800/60 border border-slate-700 text-slate-300 hover:text-white"
            >
              Default (-0.55)
            </button>
            <button
              onClick={() => onUpdateDirection(0.7, -0.7)}
              className="flex-1 text-[10px] font-mono py-1 rounded bg-slate-800/60 border border-slate-700 text-slate-300 hover:text-white"
            >
              Top-Right Sun
            </button>
            <button
              onClick={() => onUpdateDirection(0, 0)}
              className="flex-1 text-[10px] font-mono py-1 rounded bg-slate-800/60 border border-slate-700 text-slate-300 hover:text-white"
            >
              Centered Flat
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
