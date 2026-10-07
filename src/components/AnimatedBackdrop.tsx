import React, { useEffect, useRef } from 'react';
import { BackdropMode } from '../types';

interface AnimatedBackdropProps {
  mode?: BackdropMode;
  className?: string;
  showWatermark?: boolean;
}

export const AnimatedBackdrop: React.FC<AnimatedBackdropProps> = ({
  mode = 'orbs',
  className = '',
  showWatermark = true,
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    if (mode !== 'orbs') return;
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animationFrameId: number;
    let t = 0;

    const resize = () => {
      const rect = canvas.getBoundingClientRect();
      const dpr = window.devicePixelRatio || 1;
      canvas.width = rect.width * dpr;
      canvas.height = rect.height * dpr;
    };

    resize();
    window.addEventListener('resize', resize);

    const render = () => {
      const w = canvas.width;
      const h = canvas.height;
      if (w === 0 || h === 0) {
        animationFrameId = requestAnimationFrame(render);
        return;
      }

      // Base background: #060810 (rgb(6, 8, 16))
      ctx.fillStyle = 'rgb(6, 8, 16)';
      ctx.fillRect(0, 0, w, h);

      // Scale factors for canvas resolution
      const scale = Math.min(w, h) / 600;

      // Draw Orb 1: Electric Blue (#0A84FF)
      const x1 = w * (0.25 + 0.07 * Math.sin(t * 0.71));
      const y1 = h * 0.26;
      const r1 = 330 * scale;
      drawOrb(ctx, x1, y1, r1, 'rgba(10, 132, 255, 0.95)', 'rgba(10, 132, 255, 0)');

      // Draw Orb 2: Vivid Pink / Red (#FF375F)
      const x2 = w * (0.72 + 0.09 * Math.sin(t * 0.53 + 1.8));
      const y2 = h * 0.38;
      const r2 = 380 * scale;
      drawOrb(ctx, x2, y2, r2, 'rgba(255, 55, 95, 0.95)', 'rgba(255, 55, 95, 0)');

      // Draw Orb 3: Fresh Green (#30D158)
      const x3 = w * 0.50;
      const y3 = h * (0.72 + 0.06 * Math.sin(t * 0.83));
      const r3 = 430 * scale;
      drawOrb(ctx, x3, y3, r3, 'rgba(48, 209, 88, 0.95)', 'rgba(48, 209, 88, 0)');

      // Background detail watermark matching AnimatedBackdropView.kt
      if (showWatermark) {
        ctx.fillStyle = 'rgba(255, 255, 255, 0.38)';
        ctx.font = `600 ${Math.max(14, 22 * scale)}px "JetBrains Mono", monospace`;
        ctx.fillText('Move the lens over detail', 28 * scale, h * 0.86);
      }

      t += 0.012;
      animationFrameId = requestAnimationFrame(render);
    };

    render();

    return () => {
      cancelAnimationFrame(animationFrameId);
      window.removeEventListener('resize', resize);
    };
  }, [mode, showWatermark]);

  const drawOrb = (
    ctx: CanvasRenderingContext2D,
    x: number,
    y: number,
    radius: number,
    colorInner: string,
    colorOuter: string
  ) => {
    const grad = ctx.createRadialGradient(x, y, 0, x, y, radius);
    grad.addColorStop(0, colorInner);
    grad.addColorStop(1, colorOuter);
    ctx.fillStyle = grad;
    ctx.beginPath();
    ctx.arc(x, y, radius, 0, Math.PI * 2);
    ctx.fill();
  };

  if (mode === 'blue') {
    return (
      <div className={`w-full h-full bg-[#1457FF] flex flex-col items-center justify-center p-6 text-white ${className}`}>
        <div className="w-16 h-16 rounded-2xl bg-white/20 backdrop-blur-md flex items-center justify-center mb-4 text-3xl font-bold">
          B
        </div>
        <h3 className="text-xl font-bold tracking-tight">Pure Blue Environment (#1457FF)</h3>
        <p className="text-blue-100 text-sm max-w-xs text-center mt-2 opacity-80">
          Android IME native blur test: creates an intense soft cobalt haze inside the translucent glass.
        </p>
      </div>
    );
  }

  if (mode === 'red') {
    return (
      <div className={`w-full h-full bg-[#FF2D55] flex flex-col items-center justify-center p-6 text-white ${className}`}>
        <div className="w-16 h-16 rounded-2xl bg-white/20 backdrop-blur-md flex items-center justify-center mb-4 text-3xl font-bold">
          R
        </div>
        <h3 className="text-xl font-bold tracking-tight">Crimson Red Environment (#FF2D55)</h3>
        <p className="text-rose-100 text-sm max-w-xs text-center mt-2 opacity-80">
          Tests warm chromatic dispersion and deep ruby undertones diffusing through the bottom keyboard bezel.
        </p>
      </div>
    );
  }

  if (mode === 'green') {
    return (
      <div className={`w-full h-full bg-[#00C853] flex flex-col items-center justify-center p-6 text-white ${className}`}>
        <div className="w-16 h-16 rounded-2xl bg-white/20 backdrop-blur-md flex items-center justify-center mb-4 text-3xl font-bold">
          G
        </div>
        <h3 className="text-xl font-bold tracking-tight">Vivid Green Environment (#00C853)</h3>
        <p className="text-emerald-100 text-sm max-w-xs text-center mt-2 opacity-80">
          Validates high-luminance color bleed and contrast legibility against white keyboard glyphs.
        </p>
      </div>
    );
  }

  if (mode === 'dark') {
    return (
      <div className={`w-full h-full bg-[#0b0f19] flex flex-col items-center justify-center p-6 text-slate-300 ${className}`}>
        <div className="w-16 h-16 rounded-2xl bg-slate-800/80 border border-slate-700/50 flex items-center justify-center mb-4 text-3xl">
          🌙
        </div>
        <h3 className="text-xl font-bold text-white tracking-tight">Obsidian Dark (#0B0F19)</h3>
        <p className="text-slate-400 text-sm max-w-xs text-center mt-2">
          Subtle smoke glass tinting test with minimal backlighting to observe raw specular highlights.
        </p>
      </div>
    );
  }

  if (mode === 'grid') {
    return (
      <div className={`w-full h-full bg-[#0d111c] p-6 overflow-hidden flex flex-col ${className}`}>
        <div className="text-xs font-mono text-cyan-400/80 mb-2">OPTICAL ALIGNMENT & MEDIAL-AXIS VERIFICATION GRID</div>
        <div className="grid grid-cols-6 gap-2 opacity-70 flex-1">
          {Array.from({ length: 36 }).map((_, i) => (
            <div
              key={i}
              className="border border-cyan-500/30 rounded p-2 flex flex-col justify-between font-mono text-[10px] text-cyan-300/60"
            >
              <span>#{String(i + 1).padStart(2, '0')}</span>
              <div className="w-full h-px bg-cyan-500/20 my-1" />
              <span className="text-[9px] truncate">x:{(i * 17) % 100} y:{(i * 31) % 100}</span>
            </div>
          ))}
        </div>
        <div className="text-xs font-mono text-cyan-400/50 mt-2 text-right">0xQWEA0-REFRACTION-TARGET</div>
      </div>
    );
  }

  if (mode === 'photo') {
    return (
      <div className={`w-full h-full relative overflow-hidden bg-slate-900 ${className}`}>
        <div
          className="absolute inset-0 bg-cover bg-center"
          style={{
            backgroundImage: `radial-gradient(circle at 30% 40%, rgba(255,100,50,0.85), transparent 45%), radial-gradient(circle at 75% 65%, rgba(60,130,246,0.9), transparent 50%), linear-gradient(135deg, #1e1b4b 0%, #030712 100%)`,
          }}
        />
        <div className="absolute inset-0 bg-[radial-gradient(#ffffff15_1px,transparent_1px)] [background-size:16px_16px] pointer-events-none" />
        <div className="absolute top-8 left-8 right-8 text-white z-10 pointer-events-none">
          <div className="inline-block px-2.5 py-1 rounded bg-black/40 backdrop-blur-md border border-white/10 text-xs font-mono text-white/90">
            HIGH-CONTRAST BENCHMARK TARGET
          </div>
          <h2 className="text-2xl font-black mt-2 tracking-tight">Liquid Laboratory Optics</h2>
          <p className="text-sm text-slate-300 mt-1 max-w-sm">
            Observe edge compression, radial distortion, and chromatic dispersion across contrasting light and dark boundaries.
          </p>
        </div>
      </div>
    );
  }

  if (mode === 'scrolling') {
    return (
      <div className={`w-full h-full overflow-y-auto p-4 space-y-3 bg-[#080d1a] ${className}`}>
        <div className="text-xs font-mono text-blue-400 uppercase tracking-wider mb-2">Live Scrolling Content Feed</div>
        {[
          { title: 'ColorOS 15 Ambient Haze Protocol', color: 'from-blue-600 to-indigo-700', tag: 'AOSP S+' },
          { title: 'WindowManager.isCrossWindowBlurEnabled', color: 'from-purple-600 to-pink-600', tag: 'API 31' },
          { title: 'QWEA0 Bevel Normal Field Distortion', color: 'from-emerald-600 to-teal-700', tag: 'v2.0.1' },
          { title: 'Procedural AGSL Specular Highlight', color: 'from-amber-600 to-orange-700', tag: 'Specular' },
          { title: 'Battery Saver Auto-Dim Insets', color: 'from-rose-600 to-red-700', tag: 'PowerManager' },
          { title: 'Fresnel Perimeter Curvature Magnification', color: 'from-cyan-600 to-blue-700', tag: 'Fresnel' },
        ].map((item, idx) => (
          <div
            key={idx}
            className={`p-4 rounded-xl bg-gradient-to-r ${item.color} shadow-lg text-white border border-white/10 transition-transform hover:scale-[1.01]`}
          >
            <div className="flex justify-between items-start">
              <span className="text-[10px] font-mono font-bold uppercase tracking-wider px-2 py-0.5 rounded-full bg-black/30">
                {item.tag}
              </span>
              <span className="text-xs text-white/70">#0{idx + 1}</span>
            </div>
            <h4 className="font-semibold text-base mt-2">{item.title}</h4>
            <p className="text-xs text-white/80 mt-1">
              Scroll this list underneath the IME glass panel to observe real-time environmental color shifting!
            </p>
          </div>
        ))}
      </div>
    );
  }

  return (
    <div className={`w-full h-full relative overflow-hidden bg-[#060810] ${className}`}>
      <canvas ref={canvasRef} className="w-full h-full block" />
    </div>
  );
};
