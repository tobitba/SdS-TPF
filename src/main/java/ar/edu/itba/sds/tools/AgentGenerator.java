package ar.edu.itba.sds.tools;

import ar.edu.itba.sds.engine.Agent;
import ar.edu.itba.sds.engine.Civilian;
import ar.edu.itba.sds.engine.Doctor;
import ar.edu.itba.sds.engine.Zombie;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class AgentGenerator {
    private static final int MAX_INTENTOS = 1000;

    public static void generateAgents(int civilianCount, int doctorCount, Consumer<Civilian> civilianConsumer,
                                      Consumer<Doctor> doctorConsumer, Consumer<Zombie> zombieConsumer, double fieldRadius){
        double initialHumanRadius = Agent.getRmin();
        GeneratedGrid grid = new GeneratedGrid(fieldRadius);
        generateHuman(civilianCount,civilianConsumer,initialHumanRadius, fieldRadius, grid, (x,y) -> new Civilian(x,y,0,0));
        generateHuman(doctorCount,doctorConsumer,initialHumanRadius, fieldRadius, grid, (x,y) -> new Doctor(x,y,0,0));
        zombieConsumer.accept(new Zombie(0,0,0,0,false));
    }


    private static <T extends Agent> void generateHuman(
            int particleNumber,
            Consumer<T> consumer,
            double initialRadius, double fieldRadius, GeneratedGrid grid, BiFunction<Double, Double, T> agentFactory) {
        double rMin = 1.0 + initialRadius;
        double rMax = fieldRadius - 1.0 - initialRadius;
        Random random = new Random();
        random.setSeed(System.currentTimeMillis());
        double x, y, r, theta;
        Cell cell;
        double cellSize = 2 * initialRadius;
        for (int i = 0; i < particleNumber; i++) {

            int intentos = 0;
            do {
                theta = 2 * Math.PI * random.nextDouble();
                r = Math.sqrt(random.nextDouble() * (rMax * rMax - rMin * rMin) + rMin * rMin);
                x = r * Math.cos(theta);
                y = r * Math.sin(theta);
                int ci = (int) Math.floor(x / cellSize);
                int cj = (int) Math.floor(y / cellSize);
                cell = new Cell(ci,cj);
                intentos++;
            } while(grid.checkCollision(x,y,cell) && intentos < MAX_INTENTOS);

            if(intentos == MAX_INTENTOS){
                throw new RuntimeException("Could not fit humans on field. Try lowering human count");
            }
            T agent = agentFactory.apply(x, y);
            grid.addAgent(agent, cell);
            consumer.accept(agent);

        }
    }

    private static class GeneratedGrid {
        private final double cellSize;
        private final Map<Cell, List<Agent>> grid;
        private final static int[][] directions = {
                {1, 0},   // derecha
                {1, 1},   // arriba derecha
                {0, 1},   // arriba
                {-1, 1},  // arriba izquierda
                {-1, 0},  // izquierda
                {-1, -1}, // abajo izquierda
                {0, -1},  // abajo
                {1, -1},  // abajo derecha
                {0, 0}    //celda actual
        };


        public GeneratedGrid(double particleRadius) {
            this.cellSize = 2 * particleRadius;
            this.grid = new HashMap<>();
        }

        public void addAgent(Agent agent, Cell cell) {
            grid.computeIfAbsent(cell, k -> new ArrayList<>()).add(agent);
        }

        public boolean checkCollision(double x, double y, Cell cell) {
            //TODO: hacer check con radios, no solo con el centro
            for(int[] direction : directions){
                Cell neighborCell = new Cell(cell.i + direction[0], cell.j + direction[1]);
                List<Agent> agents = grid.get(neighborCell);
                if(agents != null){
                    for (Agent other : agents) {
                        double dx = x - other.getX();
                        double dy = y - other.getY();
                        double dist2 = dx * dx + dy * dy;
                        double minDist = cellSize * cellSize;
                        if (dist2 < minDist) { // d2<(2⋅r)2
                            return true; // se solapa
                        }
                    }
                }
            }
            return false;
        }


    }

    private static class Cell {
        private int i, j;

        Cell(int i, int j) {
            this.i = i;
            this.j = j;
        }

        public void setI(int i) {
            this.i = i;
        }

        public void setJ(int j) {
            this.j = j;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Cell cell && i == cell.i && j == cell.j;
        }

        @Override
        public int hashCode() {
            return Objects.hash(i, j);
        }
    }
}
