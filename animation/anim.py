# Generador de video / GIF para simulación Zombies CPM
# --------------------------------------------------
# Uso:
#   python render_simulation.py salida.txt --radius 10 --fps 30 --out animacion.gif
#   python render_simulation.py salida.txt --radius 10 --fps 60 --out animacion.mp4

import argparse
import math
import numpy as np
from pathlib import Path

import matplotlib.pyplot as plt
import imageio.v2 as imageio

# ===================== PARSER =====================
def parse_frames(path):
    frames = []
    current = []
    mode = None

    with open(path, 'r') as f:
        for raw in f:
            line = raw.strip()

            if not line:
                if current:
                    frames.append(current)
                    current = []
                mode = None
                continue

            if line in {"CIVILIANS", "DOCTORS", "ZOMBIES"}:
                mode = line
                continue

            if line.startswith("currentTime"):
                continue

            if mode is None:
                continue

            x, y, r, transforming = line.split(',')
            current.append({
                "type": mode,
                "x": float(x),
                "y": float(y),
                "r": float(r),
                "transforming": transforming == 'true'
            })

    if current:
        frames.append(current)

    return frames

# ===================== DRAW =====================
COLORS = {
    "CIVILIANS": "#999999",
    "DOCTORS": "#1f77b4",
    "ZOMBIES": "#66cc66",
    "TRANSFORMING": "#ffcc00"
}


def draw_frame(ax, agents, R):
    ax.clear()
    ax.set_aspect('equal')
    ax.set_xlim(-R, R)
    ax.set_ylim(-R, R)
    ax.axis('off')

    # recinto
    circle = plt.Circle((0, 0), R, fill=False, color='black', linewidth=2)
    ax.add_patch(circle)

    for a in agents:
        color = COLORS[a['type']]
        if a['transforming']:
            color = COLORS['TRANSFORMING']
        circ = plt.Circle((a['x'], a['y']), a['r'], color=color)
        ax.add_patch(circ)

# ===================== MAIN =====================
def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('input', type=Path)
    parser.add_argument('--radius', type=float, required=True)
    parser.add_argument('--fps', type=int, default=30)
    parser.add_argument('--out', type=Path, default=Path('animacion.gif'))
    args = parser.parse_args()

    frames = parse_frames(args.input)
    print(f"Frames cargados: {len(frames)}")

    fig, ax = plt.subplots(figsize=(6, 6))

    images = []
    for i, agents in enumerate(frames):
        draw_frame(ax, agents, args.radius)
        fig.canvas.draw()
        # Convertir el buffer RGBA de matplotlib a array numpy
        buf = fig.canvas.buffer_rgba()
        # IMPORTANTE: copiar el buffer, si no todos los frames apuntan al mismo array
        image = np.asarray(buf).copy()
        images.append(image)

        if i % 50 == 0:
            print(f"Renderizando frame {i}/{len(frames)}")

    duration = 1 / args.fps
    if args.out.suffix == '.gif':
        imageio.mimsave(args.out, images, duration=duration)
    else:
        imageio.mimsave(args.out, images, fps=args.fps)

    print(f"Archivo generado: {args.out}")


if __name__ == '__main__':
    main()
