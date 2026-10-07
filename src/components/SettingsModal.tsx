import React from 'react';
import { X, Check, Sliders, Shield, Info, Palette } from 'lucide-react';
import { OpticalParameters } from '../types';

interface SettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  params: OpticalParameters;
  onUpdateParams: (params: Partial<OpticalParameters>) => void;
  isBatterySaver: boolean;
  onToggleBatterySaver: () => void;
}

export const SettingsModal: React.FC<SettingsModalProps> = ({
  isOpen,
  onClose,
  params,
  onUpdateParams,
  isBatterySaver,
  onToggleBatterySaver,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="relative w-full max-w-md bg-[#0d121f] border border-slate-700/80 rounded-3xl p-6 shadow-2xl text-white">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 border-b border-slate-800">
          <div>
            <h3 className="text-lg font-bold">Liquid Lab Settings</h3>
            <p className="text-xs text-slate-400 font-mono">com.mangoloads.liquid.LiquidImeService</p>
          </div>
          <button
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-slate-800 flex items-center justify-center text-slate-300 hover:text-white"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Content */}
        <div className="space-y-4 py-4 max-h-[70vh] overflow-y-auto pr-1">
          {/* IME System Status */}
          <div className="p-3 rounded-xl bg-slate-900 border border-slate-800">
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-slate-300">Default Input Method</span>
              <span className="text-[10px] font-mono px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                ACTIVE
              </span>
            </div>
            <p className="text-[11px] text-slate-400 mt-1">
              Liquid Glass IME is configured and rendering with native window backdrop blur.
            </p>
          </div>

          {/* Cross Window Blur Radius */}
          <div className="p-3 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-xs font-semibold text-slate-200">Window.setBackgroundBlurRadius</span>
              <span className="text-xs font-mono text-cyan-400 font-bold">{params.blurRadius} px</span>
            </div>
            <input
              type="range"
              min="0"
              max="120"
              step="2"
              value={params.blurRadius}
              onChange={(e) => onUpdateParams({ blurRadius: parseInt(e.target.value) })}
              className="w-full accent-cyan-400 h-1.5 bg-slate-800 rounded-lg cursor-pointer"
            />
            <span className="text-[10px] text-slate-500 block">
              Native compositor blur radius passed to the OS window manager.
            </span>
          </div>

          {/* Battery Saver Policy */}
          <div className="p-3 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between">
            <div>
              <div className="text-xs font-semibold text-slate-200">Battery Saver Auto-Dim</div>
              <div className="text-[10px] text-slate-400 mt-0.5">
                Disable blur to conserve GPU cycles when device is in battery saver mode.
              </div>
            </div>
            <button
              onClick={onToggleBatterySaver}
              className={`w-12 h-6 rounded-full transition-colors p-1 flex items-center ${
                isBatterySaver ? 'bg-amber-500 justify-end' : 'bg-slate-700 justify-start'
              }`}
            >
              <div className="w-4 h-4 rounded-full bg-white shadow-md" />
            </button>
          </div>

          {/* Research & Attribution Notice */}
          <div className="p-3 rounded-xl bg-slate-950 border border-slate-800/80 text-[11px] text-slate-400 space-y-1">
            <div className="font-semibold text-slate-300 flex items-center gap-1.5">
              <Info className="w-3.5 h-3.5 text-cyan-400" />
              Attribution & Licenses
            </div>
            <p>
              • QWEA0/Liquid-Glass-Android (MIT)
              <br />
              • Kyant0/AndroidLiquidGlass (Apache-2.0)
              <br />
              • ColorOS 15 & Android 15 WindowManager APIs
            </p>
          </div>
        </div>

        {/* Footer */}
        <div className="pt-3 border-t border-slate-800 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-bold text-xs"
          >
            Apply & Close
          </button>
        </div>
      </div>
    </div>
  );
};
