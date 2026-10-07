import React, { useState, useRef, useEffect } from 'react';
import { KeyDef, AmbientState, OpticalParameters } from '../types';
import { OpticalLensShader } from './OpticalLensShader';

interface LiquidGlassKeyboardProps {
  onCommitText: (text: string) => void;
  onDeleteBackward: () => void;
  onSendEnter: () => void;
  ambientState: AmbientState;
  opticalParams: OpticalParameters;
  isNativeBlurEnabled: boolean;
  className?: string;
}

export const LiquidGlassKeyboard: React.FC<LiquidGlassKeyboardProps> = ({
  onCommitText,
  onDeleteBackward,
  onSendEnter,
  ambientState,
  opticalParams,
  isNativeBlurEnabled,
  className = '',
}) => {
  const [shift, setShift] = useState(false);
  const [numeric, setNumeric] = useState(false);
  const [pressedIndex, setPressedIndex] = useState<number | null>(null);
  const [touchPos, setTouchPos] = useState<{ x: number; y: number } | null>(null);

  const containerRef = useRef<HTMLDivElement | null>(null);
  const [dimensions, setDimensions] = useState({ width: 380, height: 286 });

  useEffect(() => {
    const updateSize = () => {
      if (containerRef.current) {
        setDimensions({
          width: containerRef.current.offsetWidth || 380,
          height: containerRef.current.offsetHeight || 286,
        });
      }
    };
    updateSize();
    window.addEventListener('resize', updateSize);
    return () => window.removeEventListener('resize', updateSize);
  }, []);

  const getRows = (): KeyDef[][] => {
    if (numeric) {
      return [
        '1234567890'.split('').map((c) => ({ label: c, action: 'TEXT', value: c, weight: 1 })),
        [
          { label: '-', action: 'TEXT', value: '-', weight: 1 },
          { label: '/', action: 'TEXT', value: '/', weight: 1 },
          { label: ':', action: 'TEXT', value: ':', weight: 1 },
          { label: ';', action: 'TEXT', value: ';', weight: 1 },
          { label: '(', action: 'TEXT', value: '(', weight: 1 },
          { label: ')', action: 'TEXT', value: ')', weight: 1 },
          { label: '$', action: 'TEXT', value: '$', weight: 1 },
          { label: '&', action: 'TEXT', value: '&', weight: 1 },
          { label: '@', action: 'TEXT', value: '@', weight: 1 },
        ],
        [
          { label: 'ABC', action: 'MODE', value: 'ABC', weight: 1.2 },
          { label: '.', action: 'TEXT', value: '.', weight: 1 },
          { label: ',', action: 'TEXT', value: ',', weight: 1 },
          { label: '?', action: 'TEXT', value: '?', weight: 1 },
          { label: '!', action: 'TEXT', value: '!', weight: 1 },
          { label: '⌫', action: 'BACKSPACE', weight: 1.2 },
        ],
        [
          { label: 'ABC', action: 'MODE', value: 'ABC', weight: 1.1 },
          { label: 'SPACE', action: 'SPACE', value: ' ', weight: 4.0 },
          { label: '↵', action: 'ENTER', weight: 1.1 },
        ],
      ];
    }

    return [
      'qwertyuiop'.split('').map((c) => ({ label: c, action: 'TEXT', value: c, weight: 1 })),
      'asdfghjkl'.split('').map((c) => ({ label: c, action: 'TEXT', value: c, weight: 1 })),
      [
        { label: '⇧', action: 'SHIFT', weight: 1.25 },
        ...'zxcvbnm'.split('').map((c) => ({ label: c, action: 'TEXT' as const, value: c, weight: 1 })),
        { label: '⌫', action: 'BACKSPACE', weight: 1.25 },
      ],
      [
        { label: '?123', action: 'MODE', weight: 1.2 },
        { label: ',', action: 'TEXT', value: ',', weight: 1 },
        { label: 'SPACE', action: 'SPACE', value: ' ', weight: 4.5 },
        { label: '.', action: 'TEXT', value: '.', weight: 1 },
        { label: '↵', action: 'ENTER', weight: 1.2 },
      ],
    ];
  };

  const rows = getRows();
  const allKeys = rows.flat();

  const handleKeyInteraction = (key: KeyDef) => {
    switch (key.action) {
      case 'TEXT':
        if (key.value) {
          const char = shift ? key.value.toUpperCase() : key.value;
          onCommitText(char);
          if (shift) setShift(false);
        }
        break;
      case 'BACKSPACE':
        onDeleteBackward();
        break;
      case 'ENTER':
        onSendEnter();
        break;
      case 'SPACE':
        onCommitText(' ');
        if (shift) setShift(false);
        break;
      case 'SHIFT':
        setShift((prev) => !prev);
        break;
      case 'MODE':
        setNumeric((prev) => !prev);
        break;
    }
  };

  const getDisplayLabel = (key: KeyDef): string => {
    if (key.action === 'TEXT' && key.value && key.value.length === 1 && shift) {
      return key.value.toUpperCase();
    }
    return key.label;
  };

  // Glass blur styling
  // Native Android uses Window.setBackgroundBlurRadius(78)
  const blurPx = isNativeBlurEnabled ? opticalParams.blurRadius : 0;

  return (
    <div
      ref={containerRef}
      className={`relative w-full h-[286px] select-none touch-none rounded-[38px] p-2 overflow-hidden transition-all duration-300 ${className}`}
      style={{
        backdropFilter: blurPx > 0 ? `blur(${blurPx}px) saturate(140%)` : 'none',
        WebkitBackdropFilter: blurPx > 0 ? `blur(${blurPx}px) saturate(140%)` : 'none',
        backgroundColor: 'rgba(255, 255, 255, 0.035)',
        boxShadow: '0 8px 32px 0 rgba(0, 0, 0, 0.37), inset 0 1px 1px 0 rgba(255, 255, 255, 0.25)',
        border: '1px solid rgba(255, 255, 255, 0.14)',
      }}
    >
      {/* Layer 1: Procedural Ambient Optical Highlight Shader */}
      <div className="absolute inset-0 pointer-events-none rounded-[38px] overflow-hidden">
        <OpticalLensShader
          width={dimensions.width}
          height={dimensions.height}
          ambientState={ambientState}
          params={opticalParams}
          interactiveTouch={touchPos}
        />
      </div>

      {/* Layer 2: Keyboard Structure & Keys */}
      <div className="relative z-10 w-full h-full flex flex-col justify-between py-1 px-1">
        {rows.map((row, rIdx) => (
          <div key={rIdx} className="flex gap-1.5 w-full h-[22%]">
            {row.map((key) => {
              const globalIdx = allKeys.indexOf(key);
              const isPressed = pressedIndex === globalIdx;
              const isShiftActive = key.action === 'SHIFT' && shift;

              return (
                <button
                  key={`${rIdx}-${key.label}`}
                  type="button"
                  style={{ flex: key.weight || 1 }}
                  onPointerDown={(e) => {
                    const rect = containerRef.current?.getBoundingClientRect();
                    if (rect) {
                      setTouchPos({ x: e.clientX - rect.left, y: e.clientY - rect.top });
                    }
                    setPressedIndex(globalIdx);
                    handleKeyInteraction(key);
                  }}
                  onPointerUp={() => {
                    setPressedIndex(null);
                    setTouchPos(null);
                  }}
                  onPointerLeave={() => {
                    setPressedIndex(null);
                    setTouchPos(null);
                  }}
                  className={`
                    relative rounded-[13px] flex items-center justify-center font-medium
                    transition-all duration-75 active:scale-[0.97]
                    border
                    ${
                      isPressed || isShiftActive
                        ? 'bg-white/25 border-white/60 shadow-[0_0_12px_rgba(255,255,255,0.35)]'
                        : 'bg-white/10 hover:bg-white/15 border-white/20'
                    }
                  `}
                >
                  {/* Internal specular edge line (0.45dp 0x15FFFFFF highlight) */}
                  <div className="absolute top-[3px] left-2 right-2 h-[1px] bg-white/25 pointer-events-none rounded-full" />

                  {/* Key label */}
                  <span
                    className={`
                      text-white font-sans tracking-wide
                      ${key.action === 'SPACE' ? 'text-[10px] tracking-widest uppercase opacity-75' : ''}
                      ${key.action === 'MODE' ? 'text-[11px] font-mono' : ''}
                      ${key.action === 'SHIFT' || key.action === 'BACKSPACE' || key.action === 'ENTER' ? 'text-[14px]' : ''}
                      ${key.action === 'TEXT' ? 'text-[16px] font-semibold' : ''}
                    `}
                  >
                    {getDisplayLabel(key)}
                  </span>
                </button>
              );
            })}
          </div>
        ))}
      </div>
    </div>
  );
};
