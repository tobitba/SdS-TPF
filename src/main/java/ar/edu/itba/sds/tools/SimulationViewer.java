package ar.edu.itba.sds.tools;
import ar.edu.itba.sds.engine.Agent;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SimulationViewer extends JPanel {

    private final List<Agent> agents;
    private final double fieldRadius;

    // pixels per meter
    private final double scale = 30;

    public SimulationViewer(List<Agent> agents, double fieldRadius) {
        this.agents = agents;
        this.fieldRadius = fieldRadius;
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        int width = getWidth();
        int height = getHeight();

        int cx = width / 2;
        int cy = height / 2;

        // ---------- DIBUJO DEL RECINTO ----------
        int R = (int) (fieldRadius * scale);

        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(cx - R, cy - R, 2 * R, 2 * R);

        // ---------- DIBUJO DE AGENTES ----------
        for (Agent a : agents) {
            int px = cx + (int) (a.getX() * scale);
            int py = cy - (int) (a.getY() * scale); // ojo: eje Y invertido

            int r = 4; // radio visual del agente

            if (a.isDoctor()) {
                g2.setColor(Color.RED);
            } else {
                g2.setColor(Color.BLUE);
            }

            g2.fillOval(px - r, py - r, 2 * r, 2 * r);
        }
    }

    // ---------- MÉTODO PARA MOSTRAR LA VENTANA ----------
    public static void show(List<Agent> agents, double fieldRadius) {
        JFrame frame = new JFrame("Simulación de Agentes");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SimulationViewer panel = new SimulationViewer(agents, fieldRadius);
        frame.add(panel);

        frame.setSize(800, 800);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
