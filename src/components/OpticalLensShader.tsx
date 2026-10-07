import React, { useEffect, useRef } from 'react';
import { AmbientState, OpticalParameters } from '../types';

interface OpticalLensShaderProps {
  width: number;
  height: number;
  ambientState: AmbientState;
  params: OpticalParameters;
  className?: string;
  sourceCanvasOrElement?: HTMLElement | null;
  interactiveTouch?: { x: number; y: number } | null;
}

export const OpticalLensShader: React.FC<OpticalLensShaderProps> = ({
  width,
  height,
  ambientState,
  params,
  className = '',
  interactiveTouch = null,
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const gl = canvas.getContext('webgl', { alpha: true, antialias: true, premultipliedAlpha: false });
    let animationFrameId: number;

    if (!gl) {
      // Fallback to 2D canvas if WebGL isn't supported
      const ctx = canvas.getContext('2d');
      if (ctx) {
        ctx.clearRect(0, 0, width, height);
        ctx.fillStyle = 'rgba(255, 255, 255, 0.08)';
        ctx.beginPath();
        ctx.roundRect(0, 0, width, height, params.bevelRadius);
        ctx.fill();
      }
      return;
    }

    // Vertex shader
    const vsSource = `
      attribute vec2 a_position;
      varying vec2 v_uv;
      void main() {
        v_uv = (a_position + 1.0) * 0.5;
        // Flip Y for texture coordinates
        v_uv.y = 1.0 - v_uv.y;
        gl_Position = vec4(a_position, 0.0, 1.0);
      }
    `;

    // Fragment shader implementing QWEA0/Kyant0 Liquid Glass optics:
    // - center clear
    // - edge bevel distortion
    // - chromatic dispersion
    // - Fresnel rim
    // - Ambient normal-driven specular highlight
    const fsSource = `
      precision mediump float;
      varying vec2 v_uv;
      
      uniform vec2 u_resolution;
      uniform vec2 u_lightDir;
      uniform float u_ambient;
      uniform float u_radius;
      uniform float u_ior;
      uniform float u_dispersion;
      uniform float u_specularPower;
      uniform float u_rimIntensity;
      uniform vec2 u_touchPos;
      uniform float u_hasTouch;

      float sdRoundRect(vec2 p, vec2 b, float r) {
        vec2 q = abs(p) - b + r;
        return min(max(q.x, q.y), 0.0) + length(max(q, vec2(0.0))) - r;
      }

      void main() {
        vec2 p = gl_FragCoord.xy;
        // Convert to centered coords
        vec2 halfRes = u_resolution * 0.5;
        vec2 b = halfRes - 1.0;
        
        float d = sdRoundRect(p - halfRes, b, u_radius);
        if (d > 0.0) {
          discard;
        }

        // Numerical gradient to get surface normal
        float eps = 1.2;
        float dx = sdRoundRect(p - halfRes + vec2(eps, 0.0), b, u_radius) -
                   sdRoundRect(p - halfRes - vec2(eps, 0.0), b, u_radius);
        float dy = sdRoundRect(p - halfRes + vec2(0.0, eps), b, u_radius) -
                   sdRoundRect(p - halfRes - vec2(0.0, eps), b, u_radius);

        vec2 n = normalize(vec2(dx, dy) + 0.0001);

        // Normalized distance from edge (0 at edge, 1 deep inside)
        float edgeNorm = clamp(-d / max(u_radius, 1.0), 0.0, 1.0);
        
        // Edge curvature weight: center is clear (0), edge bevel has strong gradient
        float bevelWidth = min(u_radius * 0.75, 45.0);
        float edgeDistortion = 1.0 - smoothstep(0.0, bevelWidth, -d);

        // Touch deformation ripple
        if (u_hasTouch > 0.5) {
          float touchDist = length(gl_FragCoord.xy - u_touchPos);
          if (touchDist < 70.0) {
            float ripple = sin(touchDist * 0.15) * (1.0 - touchDist / 70.0) * 0.4;
            n += normalize(gl_FragCoord.xy - u_touchPos) * ripple;
            edgeDistortion = max(edgeDistortion, abs(ripple));
          }
        }

        // Lighting calculation matching Android AmbientLightGlassView
        vec2 l = normalize(u_lightDir + vec2(0.0, -0.08));
        float ndl = max(dot(n, l), 0.0);
        float spec = pow(ndl, u_specularPower);
        
        // Rim Fresnel response: rim = edge^1.55
        float rim = pow(edgeDistortion, 1.55) * u_rimIntensity;

        // Physical ambient intensity matching log curve
        float lightIntensity = mix(0.12, 0.42, u_ambient);

        // Specular highlight + rim alpha
        float highlightAlpha = (spec * 0.48 + rim * 0.035) * lightIntensity;

        // Base glass tint: ultra transparent in center, subtle milky sheen at beveled border
        float glassBaseAlpha = mix(0.04, 0.16, edgeDistortion);

        // Chromatic dispersion shimmer (RGB offset)
        vec3 dispersionColor = vec3(
          0.95 + 0.05 * sin(edgeDistortion * 6.28),
          0.97,
          1.00 + 0.08 * cos(edgeDistortion * 6.28)
        ) * (u_dispersion * edgeDistortion * 0.25);

        vec3 finalColor = vec3(1.0) + dispersionColor;
        float finalAlpha = clamp(glassBaseAlpha + highlightAlpha, 0.0, 0.95);

        gl_FragColor = vec4(finalColor * finalAlpha, finalAlpha);
      }
    `;

    const createShader = (type: number, source: string) => {
      const shader = gl.createShader(type);
      if (!shader) return null;
      gl.shaderSource(shader, source);
      gl.compileShader(shader);
      if (!gl.getShaderParameter(shader, gl.COMPILE_STATUS)) {
        console.error('Shader compile error:', gl.getShaderInfoLog(shader));
        gl.deleteShader(shader);
        return null;
      }
      return shader;
    };

    const vertShader = createShader(gl.VERTEX_SHADER, vsSource);
    const fragShader = createShader(gl.FRAGMENT_SHADER, fsSource);
    if (!vertShader || !fragShader) return;

    const program = gl.createProgram();
    if (!program) return;
    gl.attachShader(program, vertShader);
    gl.attachShader(program, fragShader);
    gl.linkProgram(program);

    if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
      console.error('Program link error:', gl.getProgramInfoLog(program));
      return;
    }

    const posBuffer = gl.createBuffer();
    gl.bindBuffer(gl.ARRAY_BUFFER, posBuffer);
    gl.bufferData(
      gl.ARRAY_BUFFER,
      new Float32Array([
        -1.0, -1.0,
         1.0, -1.0,
        -1.0,  1.0,
        -1.0,  1.0,
         1.0, -1.0,
         1.0,  1.0,
      ]),
      gl.STATIC_DRAW
    );

    const aPosLoc = gl.getAttribLocation(program, 'a_position');
    const uResLoc = gl.getUniformLocation(program, 'u_resolution');
    const uLightDirLoc = gl.getUniformLocation(program, 'u_lightDir');
    const uAmbientLoc = gl.getUniformLocation(program, 'u_ambient');
    const uRadiusLoc = gl.getUniformLocation(program, 'u_radius');
    const uIorLoc = gl.getUniformLocation(program, 'u_ior');
    const uDispersionLoc = gl.getUniformLocation(program, 'u_dispersion');
    const uSpecPowerLoc = gl.getUniformLocation(program, 'u_specularPower');
    const uRimIntensityLoc = gl.getUniformLocation(program, 'u_rimIntensity');
    const uTouchPosLoc = gl.getUniformLocation(program, 'u_touchPos');
    const uHasTouchLoc = gl.getUniformLocation(program, 'u_hasTouch');

    const render = () => {
      const dpr = window.devicePixelRatio || 1;
      const displayWidth = Math.floor(width * dpr);
      const displayHeight = Math.floor(height * dpr);

      if (canvas.width !== displayWidth || canvas.height !== displayHeight) {
        canvas.width = displayWidth;
        canvas.height = displayHeight;
      }

      gl.viewport(0, 0, canvas.width, canvas.height);
      gl.clearColor(0.0, 0.0, 0.0, 0.0);
      gl.clear(gl.COLOR_BUFFER_BIT);

      gl.useProgram(program);

      gl.enable(gl.BLEND);
      gl.blendFunc(gl.ONE, gl.ONE_MINUS_SRC_ALPHA);

      gl.enableVertexAttribArray(aPosLoc);
      gl.bindBuffer(gl.ARRAY_BUFFER, posBuffer);
      gl.vertexAttribPointer(aPosLoc, 2, gl.FLOAT, false, 0, 0);

      gl.uniform2f(uResLoc, canvas.width, canvas.height);
      gl.uniform2f(uLightDirLoc, ambientState.lightX, ambientState.lightY);
      gl.uniform1f(uAmbientLoc, ambientState.intensity);
      gl.uniform1f(uRadiusLoc, params.bevelRadius * dpr);
      gl.uniform1f(uIorLoc, params.ior);
      gl.uniform1f(uDispersionLoc, params.chromaticDispersion);
      gl.uniform1f(uSpecPowerLoc, params.specularPower);
      gl.uniform1f(uRimIntensityLoc, params.rimIntensity);

      if (interactiveTouch) {
        gl.uniform2f(uTouchPosLoc, interactiveTouch.x * dpr, (height - interactiveTouch.y) * dpr);
        gl.uniform1f(uHasTouchLoc, 1.0);
      } else {
        gl.uniform2f(uTouchPosLoc, 0.0, 0.0);
        gl.uniform1f(uHasTouchLoc, 0.0);
      }

      gl.drawArrays(gl.TRIANGLES, 0, 6);
    };

    render();

    return () => {
      cancelAnimationFrame(animationFrameId);
      gl.deleteProgram(program);
      gl.deleteShader(vertShader);
      gl.deleteShader(fragShader);
      gl.deleteBuffer(posBuffer);
    };
  }, [width, height, ambientState, params, interactiveTouch]);

  return (
    <canvas
      ref={canvasRef}
      style={{ width: `${width}px`, height: `${height}px` }}
      className={`pointer-events-none ${className}`}
    />
  );
};
