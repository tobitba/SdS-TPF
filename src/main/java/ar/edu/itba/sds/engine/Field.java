package ar.edu.itba.sds.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ar.edu.itba.sds.tools.AgentGenerator;
import ar.edu.itba.sds.tools.Time;

public class Field implements Iterable<Time> {

    private final static int X = 0;
    private final static int Y = 1;
    private final static int A_H = 4;
    private final static int B_H = 1;
    private final static int A_Z = 8;
    private final static int B_Z = 4;


    private final List<Civilian> civilians = new ArrayList<>();
    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private final double borderRadius;
    private final double maxTime;
    private double currentTime = 0;

    public Field(double borderRadius, int civilianCount, int doctorCount, double maxTime) {
        this.borderRadius = borderRadius;
        AgentGenerator.generateAgents(
                civilianCount,
                doctorCount, civilians::add,
                doctors::add, zombies::add, borderRadius);
        this.maxTime = maxTime;
    }

    public Time getCurrentTime() {
        return new Time(currentTime, civilians, zombies, doctors);
    }

    @Override
    public Iterator<Time> iterator() {
        return new Iterator<>() {
            private final static double DT = 0.8;

            @Override
            public boolean hasNext() {
                return currentTime < maxTime + DT;
            }

            @Override
            public Time next() {
                double[][] nc = new double[civilians.size()][2];
                double[][] nd = new double[doctors.size()][2];
                double[][] nz = new double[zombies.size()][2];

                for (int i = 0; i < civilians.size(); i++) {
                    Civilian c1 = civilians.get(i);
                    //Interaccion con otros civiles
                    for (int j = i + 1; j < civilians.size(); j++) {
                        Civilian c2 = civilians.get(j);

                        double[] n = getInteraction(c2, c1, A_H, B_H);

                        nc[i][X] -= n[X];
                        nc[i][Y] -= n[Y];
                        nc[j][X] += n[X];
                        nc[j][Y] += n[Y];


                    }
                    //Interaccion con doctores (misma logica anterior, tengo que evitarlos)
                    for (int j = 0; j < doctors.size(); j++) {
                        Doctor d = doctors.get(j);
                        double[] n = getInteraction(d, c1, A_H, B_H);

                        nc[i][X] -= n[X];
                        nc[i][Y] -= n[Y];
                        nd[j][X] += n[X];
                        nd[j][Y] += n[Y];

                    }
                    //Interaccion con zombies
                    for (Zombie z : zombies) {
                        double[] n = getInteraction(z, c1, A_Z, B_Z);
                        nc[i][X] -= n[X];
                        nc[i][Y] -= n[Y];
                        if (z.getDistanceToTarget() > n[2]) {
                            z.setDistanceToTarget(n[2]);
                            z.setTargetDirection(new double[]{n[3], n[4]}); //sisi ya se que esta feo.... despues vemos como lo mejoramos
                        }
                    }

                    //Interacción con la pared
                    double x = c1.getX();
                    double y = c1.getY();
                    double r = Math.sqrt(x * x + y * y);
                    if (r < 1e-9)
                        continue;
                    double distToWall = borderRadius - r;

                    double distXToWall = distToWall * (x / r); // dist * cos theta
                    double distYToWall = distToWall * (y / r); // dist * sin theta

                    double ex = distXToWall / distToWall;
                    double ey = distYToWall / distToWall;

                    nc[i][X] -= ex * A_Z * Math.exp(-distToWall / B_H);
                    nc[i][Y] -= ey * A_Z * Math.exp(-distToWall / B_H);

                }

                for (int i = 0; i < doctors.size(); i++) {
                    Doctor d = doctors.get(i);
                    for (int j = i + 1; j < doctors.size(); j++) {
                        Doctor d2 = doctors.get(j);
                        double[] n = getInteraction(d2, d, A_H, B_H);

                        nd[i][X] -= n[X];
                        nd[i][Y] -= n[Y];
                        nd[j][X] += n[X];
                        nd[j][Y] += n[Y];
                    }
                    double nearestZombieDistance = Double.MAX_VALUE;
                    double nearestZombieDirX = 0;
                    double nearestZombieDirY = 0;
                    for (int j = 0; j < zombies.size(); j++) {
                        Zombie z = zombies.get(j);
                        double[] interaction = getInteraction(z, d, A_Z, B_Z); //TODO: Revisar que A y B ponemos aca
                        double distance = interaction[2];
                        if (distance < nearestZombieDistance) {
                            double dx = d.getX() - z.getX();
                            double dy = d.getY() - z.getY();
                            nearestZombieDistance = distance;
                            nearestZombieDirX = dx / distance;
                            nearestZombieDirY = dy / distance;
                        }
                        nz[j][X] += interaction[X];
                        nz[j][Y] += interaction[Y];
                    }
                    nd[i][X] += nearestZombieDirX; //TODO: Acá usamos la dirección, tenemos que calcular el nd y se suma eso
                    nd[i][Y] += nearestZombieDirY;

                    //Interacción con la pared
                    double x = d.getX();
                    double y = d.getY();
                    double r = Math.sqrt(x * x + y * y);
                    if (r < 1e-9)
                        continue;
                    double distToWall = borderRadius - r;

                    double distXToWall = distToWall * (x / r); // dist * cos theta
                    double distYToWall = distToWall * (y / r); // dist * sin theta

                    double ex = distXToWall / distToWall;
                    double ey = distYToWall / distToWall;

                    nd[i][X] -= ex * A_Z * Math.exp(-distToWall / B_H);
                    nd[i][Y] -= ey * A_Z * Math.exp(-distToWall / B_H);

                }

                for (int i = 0; i < zombies.size(); i++) {
                    Zombie z = zombies.get(i);
                    nz[i][X] += z.getTargetDirection()[X];
                    nz[i][Y] += z.getTargetDirection()[Y];
                    //Interacción con la pared
                    double x = z.getX();
                    double y = z.getY();
                    double r = Math.sqrt(x * x + y * y);
                    if (r < 1e-9)
                        continue;
                    double distToWall = borderRadius - r;

                    double distXToWall = distToWall * (x / r); // dist * cos theta
                    double distYToWall = distToWall * (y / r); // dist * sin theta

                    double ex = distXToWall / distToWall;
                    double ey = distYToWall / distToWall;

                    nz[i][X] -= ex * A_Z * Math.exp(-distToWall / B_H);
                    nz[i][Y] -= ey * A_Z * Math.exp(-distToWall / B_H);

                }

                /// TODO: Acá abajo evaluar contactos
                for (int i = 0; i < civilians.size(); i++) {
                    civilians.get(i).move(DT, new double[]{nc[i][X], nc[i][Y]}, false);
                }
                for (int i = 0; i < doctors.size(); i++) {
                    doctors.get(i).move(DT, new double[]{nd[i][X], nd[i][Y]}, false);
                }
                for (int i = 0; i < zombies.size(); i++) {
                    zombies.get(i).move(DT, new double[]{nz[i][X], nz[i][Y]}, false);
                }
                currentTime += DT;
                return new Time(currentTime, civilians, zombies, doctors);
            }


            private double[] getInteraction(Agent targetAgent, Agent sourceAgent, int a, int b) {
                double dx = targetAgent.getX() - sourceAgent.getX(); //Esto por ahi se pouede poner como metodo en Agent
                double dy = targetAgent.getY() - sourceAgent.getY();
                double modulus = Math.sqrt(dx * dx + dy * dy);
                double ex = dx / modulus;
                double ey = dy / modulus;

                double nx = ex * a * Math.exp(-modulus / b);
                double ny = ey * a * Math.exp(-modulus / b);
                return new double[]{nx, ny, modulus, ex, ey};
            }
        };

    }
}
