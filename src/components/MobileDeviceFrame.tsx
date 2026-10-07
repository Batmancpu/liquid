import React, { useState, useRef } from 'react';
import { AnimatedBackdrop } from './AnimatedBackdrop';
import { LiquidGlassKeyboard } from './LiquidGlassKeyboard';
import { BackdropMode, AmbientState, OpticalParameters } from '../types';
import { Settings, Sparkles, Sliders, ChevronDown, RefreshCw } from 'lucide-react';

interface MobileDeviceFrameProps {
  ambientState: AmbientState;
  opticalParams: OpticalParameters;
  isNativeBlurEnabled: boolean;
  backdropMode: BackdropMode;
  onOpenSettings: () => void;
  onToggleBatterySaver?: () => void;
  isBatterySaver?: boolean;
}

export const MobileDeviceFrame: React.FC<MobileDeviceFrameProps> = ({
  ambientState,
  opticalParams,
  backdropMode,
  isNativeBlurEnabled,
  onOpenSettings,
  onToggleBatterySaver,
  isBatterySaver = false,
}) => {
  const [inputText, setInputText] = useState('Liquid Glass in ColorOS 15');
  const [isKeyboardOpen, setIsKeyboardOpen] = useState(true);
  const [inputBackground, setInputBackground] = useState<'blue' | 'red' | 'green' | 'dark' | 'transparent'>('blue');

  const inputBgClass = {
    blue: 'bg-[#1457FF]',
    red: 'bg-[#FF2D55]',
    green: 'bg-[#00C853]',
    dark: 'bg-[#1a2030]',
    transparent: 'bg-black/40 backdrop-blur-md',
  }[inputBackground];

  const handleCommitText = (text: string) => {
    setInputText((prev) => prev + text);
  };

  const handleDeleteBackward = () => {
    setInputText((prev) => prev.slice(0, -1));
  };

  const handleSendEnter = () => {
    setInputText((prev) => prev + '\n');
  };

  return (
    <div className="relative w-full max-w-[410px] h-[780px] bg-[#000000] rounded-[52px] p-3 shadow-[0_25px_60px_-15px_rgba(0,0,0,0.9),0_0_0_12px_#181a20,0_0_0_13px_#2d323f] flex flex-col overflow-hidden select-none border border-slate-700/50">
      {/* Front camera punch-hole */}
      <div className="absolute top-5 left-1/2 -translate-x-1/2 w-4 h-4 bg-black rounded-full border border-slate-800 z-50 flex items-center justify-center">
        <div className="w-1.5 h-1.5 rounded-full bg-blue-950/60 ring-1 ring-cyan-900/30" />
      </div>

      {/* Screen area */}
      <div className="relative w-full h-full rounded-[42px] overflow-hidden bg-[#060810] flex flex-col justify-between">
        {/* Animated backdrop view covering entire activity */}
        <div className="absolute inset-0 z-0">
          <AnimatedBackdrop mode={backdropMode} showWatermark={true} />
        </div>

        {/* Top bar status & Info indicator matching MainActivity */}
        <div className="relative z-20 pt-4 px-6 flex justify-between items-center text-xs text-white/80">
          <span className="font-mono text-[11px] font-semibold tracking-wider">09:41</span>
          <div className="flex items-center gap-2">
            <span className="text-[10px] px-1.5 py-0.5 rounded bg-white/10 font-mono">OPPO Reno10</span>
            <div className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
          </div>
        </div>

        {/* Top Info Overlay from MainActivity.kt */}
        <div className="relative z-20 px-5 pt-3">
          <div className="p-2.5 rounded-xl bg-black/50 backdrop-blur-md border border-white/15 text-white shadow-lg">
            <div className="text-[10px] font-mono uppercase tracking-wider text-cyan-300 font-semibold mb-0.5">
              Laboratory Diagnostics
            </div>
            <div className="text-xs text-white/90 leading-tight font-medium">
              Phase 1: optical refraction
              <br />
              Phase 2: native cross-window ambient haze
            </div>
            <div className="mt-1.5 pt-1.5 border-t border-white/10 flex items-center justify-between text-[10px] text-white/60">
              <span>Sensor: {Math.round(ambientState.lux)} lx</span>
              <span>Tilt: X:{ambientState.lightX.toFixed(2)} Y:{ambientState.lightY.toFixed(2)}</span>
            </div>
          </div>
        </div>

        {/* Middle Interactive Content / Text Field Switcher */}
        <div className="relative z-20 px-5 flex-1 flex flex-col justify-end pb-3">
          {/* Ambient color switcher bar */}
          <div className="mb-2 flex items-center justify-between bg-black/40 backdrop-blur-md p-1.5 rounded-xl border border-white/10">
            <span className="text-[10px] font-mono text-white/70 pl-1">Test Field Haze:</span>
            <div className="flex gap-1.5">
              {(['blue', 'red', 'green', 'dark'] as const).map((color) => (
                <button
                  key={color}
                  onClick={() => setInputBackground(color)}
                  className={`w-5 h-5 rounded-full border transition-all ${
                    inputBackground === color ? 'ring-2 ring-white scale-110' : 'opacity-70 hover:opacity-100'
                  } ${
                    color === 'blue'
                      ? 'bg-[#1457FF] border-blue-400'
                      : color === 'red'
                      ? 'bg-[#FF2D55] border-rose-400'
                      : color === 'green'
                      ? 'bg-[#00C853] border-emerald-400'
                      : 'bg-[#1a2030] border-slate-500'
                  }`}
                  title={`Switch test field to ${color}`}
                />
              ))}
            </div>
          </div>

          {/* Test EditText from MainActivity.kt */}
          <div
            onClick={() => setIsKeyboardOpen(true)}
            className={`cursor-pointer rounded-2xl p-4 transition-all duration-200 border border-white/20 shadow-xl ${inputBgClass} min-h-[82px] flex flex-col justify-between`}
          >
            <div className="text-xs text-white/70 font-mono flex justify-between">
              <span>Active Field (Color bleeds into IME)</span>
              {isKeyboardOpen ? (
                <span className="text-white bg-white/20 px-1.5 py-0.2 rounded text-[10px]">IME Active</span>
              ) : (
                <span className="text-cyan-300 underline text-[10px]">Tap to open IME</span>
              )}
            </div>
            <p className="text-white text-base font-medium break-words mt-1">
              {inputText || (
                <span className="text-white/60 italic">Tap here to test the glass keyboard...</span>
              )}
            </p>
          </div>

          {/* Settings button from MainActivity.kt */}
          <div className="mt-2.5 flex gap-2">
            <button
              onClick={onOpenSettings}
              className="flex-1 h-[46px] rounded-xl bg-white/10 hover:bg-white/15 active:bg-white/20 border border-white/15 backdrop-blur-md text-white font-medium text-xs flex items-center justify-center gap-1.5 transition-colors"
            >
              <Settings className="w-3.5 h-3.5 text-cyan-400" />
              <span>Keyboard Settings</span>
            </button>
            <button
              onClick={() => setIsKeyboardOpen(!isKeyboardOpen)}
              className="px-3.5 h-[46px] rounded-xl bg-white/10 hover:bg-white/15 border border-white/15 backdrop-blur-md text-white/90 text-xs flex items-center justify-center"
              title={isKeyboardOpen ? 'Hide IME' : 'Show IME'}
            >
              <ChevronDown
                className={`w-4 h-4 transition-transform duration-200 ${isKeyboardOpen ? '' : 'rotate-180'}`}
              />
            </button>
          </div>
        </div>

        {/* Bottom Liquid Glass IME Keyboard Area */}
        {isKeyboardOpen && (
          <div className="relative z-30 pb-2 px-1.5 pt-1 animate-in fade-in slide-in-from-bottom-6 duration-300">
            <LiquidGlassKeyboard
              onCommitText={handleCommitText}
              onDeleteBackward={handleDeleteBackward}
              onSendEnter={handleSendEnter}
              ambientState={ambientState}
              opticalParams={opticalParams}
              isNativeBlurEnabled={isNativeBlurEnabled && !isBatterySaver}
            />
          </div>
        )}

        {/* Android bottom navigation pill */}
        <div className="relative z-40 w-full pb-2 flex justify-center items-center pointer-events-none">
          <div className="w-28 h-1 bg-white/40 rounded-full" />
        </div>
      </div>
    </div>
  );
};
