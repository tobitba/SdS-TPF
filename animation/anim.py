import argparse
import cv2
import numpy as np
import sys
import os

# ================= CONFIGURACIÓN =================
COLORS = {
    "CIVILIANS": (153, 153, 153),  # Gris
    "DOCTORS": (180, 119, 31),     # Azul (BGR)
    "ZOMBIES": (102, 204, 102),    # Verde (BGR)
    "TRANSFORMING": (0, 204, 255)  # Naranja (BGR)
}
BACKGROUND_COLOR = (255, 255, 255) # Blanco
WINDOW_SIZE = 800

def is_time_line(line):
    """Detecta si una línea es solo un número (el tiempo)."""
    try:
        # Si no tiene comas y se puede convertir a float, es el tiempo
        if ',' not in line:
            float(line)
            return True
        return False
    except ValueError:
        return False

def process_simulation(input_file, output_file, field_radius, fps):
    if not os.path.exists(input_file):
        print(f"❌ Error: No se encuentra el archivo {input_file}")
        return

    # Escala automática
    scale = (WINDOW_SIZE / 2) / (field_radius * 1.1)
    center_offset = WINDOW_SIZE / 2

    # Configuración de video
    # mp4v es compatible con la mayoría de los reproductores
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    out = cv2.VideoWriter(str(output_file), fourcc, fps, (WINDOW_SIZE, WINDOW_SIZE))

    print(f"🎬 Procesando: {input_file}")
    print(f"   Salida: {output_file}")

    # Lienzo en blanco
    canvas = np.full((WINDOW_SIZE, WINDOW_SIZE, 3), BACKGROUND_COLOR, dtype=np.uint8)

    current_agents = []
    mode = None
    frames_count = 0

    # Leemos el archivo línea por línea
    with open(input_file, 'r') as f:
        for line in f:
            line = line.strip()
            if not line: continue

            # 1. DETECCIÓN DE CAMBIO DE FRAME
            # Si la línea es un número (ej: "0.0", "0.05"), es un nuevo frame.
            if is_time_line(line):
                # Si ya teníamos agentes acumulados del frame anterior, dibujamos y guardamos
                if current_agents or frames_count > 0: # frames_count > 0 asegura procesar frames vacíos si los hubiera
                    if current_agents:
                        draw_frame(out, canvas, current_agents, field_radius, scale, center_offset)

                    # Guardamos frame (incluso si no hubo agentes, se guarda el canvas anterior/limpio)
                    # Nota: La lógica original dibujaba al encontrar el tiempo SIGUIENTE.
                    # Aquí dibujamos lo que acumulamos hasta ahora.

                    frames_count += 1
                    if frames_count % 50 == 0:
                        sys.stdout.write(f"\r   Frames procesados: {frames_count}")
                        sys.stdout.flush()

                    # Resetear para el nuevo frame
                    current_agents = []
                    canvas[:] = BACKGROUND_COLOR # Limpiar pantalla

                # (Opcional) Podrías guardar el tiempo actual si lo necesitas mostrar en pantalla
                # current_time = float(line)
                continue

            # 2. DETECCIÓN DE TIPO
            if line in {"CIVILIANS", "DOCTORS", "ZOMBIES"}:
                mode = line
                continue

            # 3. PARSEO DE AGENTES
            if mode:
                # Esperamos x,y,r,transforming
                parts = line.split(',')
                if len(parts) >= 3:
                    try:
                        agent = {
                            'x': float(parts[0]),
                            'y': float(parts[1]),
                            'r': float(parts[2]),
                            'type': mode,
                            'trans': False
                        }
                        # Intentar leer el booleano de transformación si existe
                        if len(parts) >= 4:
                            agent['trans'] = (parts[3].strip().lower() == 'true')

                        current_agents.append(agent)
                    except ValueError:
                        pass # Ignorar líneas corruptas

        # 4. PROCESAR EL ÚLTIMO FRAME (que quedó pendiente al terminar el archivo)
        if current_agents:
            draw_frame(out, canvas, current_agents, field_radius, scale, center_offset)
            frames_count += 1
            print(f"\r   Frames procesados: {frames_count}")

    out.release()
    print(f"\n✅ Video generado correctamente.")

def draw_frame(video_writer, img, agents, R, scale, offset):
    # Dibujar Recinto (Círculo negro)
    r_pixel = int(R * scale)
    cv2.circle(img, (int(offset), int(offset)), r_pixel, (0,0,0), 2)

    # Dibujar Agentes
    for a in agents:
        px = int(a['x'] * scale + offset)
        py = int(-a['y'] * scale + offset) # Invertir eje Y
        pr = int(max(2, a['r'] * scale))   # Radio mínimo

        # Color
        color = COLORS["TRANSFORMING"] if a['trans'] else COLORS.get(a['type'], (0,0,0))

        cv2.circle(img, (px, py), pr, color, -1)

    # Escribir frame al video
    video_writer.write(img)

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('input', help="Archivo dynamicOutput.txt")
    parser.add_argument('--radius', type=float, default=11.0)
    parser.add_argument('--fps', type=int, default=30)
    parser.add_argument('--out', default='simulacion.mp4')

    args = parser.parse_args()

    # Ejecutar
    process_simulation(args.input, args.out, args.radius, args.fps)