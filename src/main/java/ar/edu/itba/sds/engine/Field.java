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
    private final static int A_W = 8;
    private final static int B_W = 1;


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
            private final static double DT = 0.01875;

            @Override
            public boolean hasNext() {
                return currentTime < maxTime + DT && !zombies.isEmpty() && (!civilians.isEmpty() || !doctors.isEmpty());
            }

            @Override
            public Time next() {
                double[][] nc = new double[civilians.size()][2];
                double[][] nd = new double[doctors.size()][2];
                double[][] nz = new double[zombies.size()][2];

                // CIVILES
                for (int i = 0; i < civilians.size(); i++) {
                    Civilian c1 = civilians.get(i);
                    //Interaccion con otros civiles
                    for (int j = i + 1; j < civilians.size(); j++) {
                        Civilian c2 = civilians.get(j);
                        if (c1.isFighting()) {
                            double[] n = getInteraction(c2, c1, A_Z, B_Z);
                            nc[j][X] += n[X];
                            nc[j][Y] += n[Y];
                            continue;
                        }
                        if (c2.isFighting()) {
                            double[] n = getInteraction(c2, c1, A_Z, B_Z);
                            nc[i][X] -= n[X];
                            nc[i][Y] -= n[Y];
                            continue;
                        }
                        double[] n = getInteraction(c2, c1, A_H, B_H);

                        nc[i][X] -= n[X];
                        nc[i][Y] -= n[Y];
                        nc[j][X] += n[X];
                        nc[j][Y] += n[Y];

                    }
                    //Interaccion con doctores (misma logica anterior, tengo que evitarlos)
                    for (int j = 0; j < doctors.size(); j++) {
                        Doctor d = doctors.get(j);
                        if (c1.isFighting()) {
                            double[] n = getInteraction(d, c1, A_Z, B_Z);
                            nd[j][X] += n[X];
                            nd[j][Y] += n[Y];
                            continue;
                        }
                        if (d.isFighting()) {
                            double[] n = getInteraction(d, c1, A_Z, B_Z);
                            nc[i][X] -= n[X];
                            nc[i][Y] -= n[Y];
                            continue;
                        }
                        double[] n = getInteraction(d, c1, A_H, B_H);

                        nc[i][X] -= n[X];
                        nc[i][Y] -= n[Y];
                        nd[j][X] += n[X];
                        nd[j][Y] += n[Y];

                    }
                    //Interaccion con zombies
                    for (int j = 0; j < zombies.size(); j++) {
                        Zombie z = zombies.get(j);
                        if (c1.isFighting()) {
                            double[] n = getInteraction(z, c1, A_Z, B_Z);
                            nz[j][X] += n[X];
                            nz[j][Y] += n[Y];
                            continue;
                        }
                        if (z.isFighting()) {
                            double[] n = getInteraction(z, c1, A_Z, B_Z);
                            nc[i][X] -= n[X];
                            nc[i][Y] -= n[Y];
                            continue;
                        }
                        double[] n = getInteraction(z, c1, A_Z, B_Z);
                        nc[i][X] -= n[X];
                        nc[i][Y] -= n[Y];
                        if (z.getDistanceToTarget() > n[2]) {
                            z.setDistanceToTarget(n[2]);
                            z.setTargetDirection(new double[]{-n[3], -n[4]});
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

                    nc[i][X] -= ex * A_W * Math.exp(-distToWall / B_W);
                    nc[i][Y] -= ey * A_W * Math.exp(-distToWall / B_W);

                }

                // DOCTORES
                for (int i = 0; i < doctors.size(); i++) {
                    Doctor d = doctors.get(i);
                    for (int j = i + 1; j < doctors.size(); j++) {
                        Doctor d2 = doctors.get(j);
                        if (d.isFighting()) {
                            double[] n = getInteraction(d2, d, A_Z, B_Z);
                            nd[j][X] += n[X];
                            nd[j][Y] += n[Y];
                            continue;
                        }
                        if (d2.isFighting()) {
                            double[] n = getInteraction(d2, d, A_Z, B_Z);
                            nd[i][X] -= n[X];
                            nd[i][Y] -= n[Y];
                            continue;
                        }
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
                        if (d.isFighting()) {
                            double[] n = getInteraction(z, d, A_Z, B_Z);
                            nz[j][X] += n[X];
                            nz[j][Y] += n[Y];
                            continue;
                        }
                        if (z.isFighting()) {
                            double[] n = getInteraction(z, d, A_Z, B_Z);
                            nd[i][X] -= n[X];
                            nd[i][Y] -= n[Y];
                            continue;
                        double[] interaction = getInteraction(z, d, A_Z, B_Z);
                        double distance = interaction[2];
                        if (distance < nearestZombieDistance) {
                            double dx = z.getX() - d.getX();
                            double dy = z.getY() - d.getY();
                            nearestZombieDistance = distance;
                            nearestZombieDirX = dx / distance;
                            nearestZombieDirY = dy / distance;
                        }
                        nz[j][X] += interaction[X];
                        nz[j][Y] += interaction[Y];
                    }
                    nd[i][X] += nearestZombieDirX * A_Z * Math.exp(- nearestZombieDistance / B_Z);
                    nd[i][Y] += nearestZombieDirY * A_Z * Math.exp(- nearestZombieDistance / B_Z);

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

                    nd[i][X] -= ex * A_W * Math.exp(-distToWall / B_W);
                    nd[i][Y] -= ey * A_W * Math.exp(-distToWall / B_W);

                }

                // ZOMBIES
                for (int i = 0; i < zombies.size(); i++) {
                    Zombie z = zombies.get(i);
                    if (! z.isFighting()) {

                        nz[i][X] += z.getTargetDirection()[X] * A_Z;
                        nz[i][Y] += z.getTargetDirection()[Y] * A_Z;
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

                    nz[i][X] -= ex * A_W * Math.exp(-distToWall/ B_W);
                    nz[i][Y] -= ey * A_W * Math.exp(-distToWall/ B_W);
                    for(int j = i + 1; j < zombies.size(); j++){
                        Zombie z2 = zombies.get(j);
                        if (z.isFighting()) {
                            double[] n = getInteraction(z2, z, A_Z, B_Z);
                            nz[j][X] += n[X];
                            nz[j][Y] += n[Y];
                            continue;
                        }
                        if (z2.isFighting()) {
                            double[] n = getInteraction(z2, z, A_Z, B_Z);
                            nz[i][X] -= n[X];
                            nz[i][Y] -= n[Y];
                            continue;
                        }
                        double[] n = getInteraction(z2, z, A_H, B_H);

                        nz[i][X] -= n[X];
                        nz[i][Y] -= n[Y];
                        nz[j][X] += n[X];
                        nz[j][Y] += n[Y];
                    }

                }

                double noiseAmp = 0.052;
                List<Civilian> toRemoveCiv = new ArrayList<>();
                List<Doctor> toRemoveDoc = new ArrayList<>();
                List<Zombie> toRemoveZom = new ArrayList<>();
                for (int i = 0; i < civilians.size(); i++) {
                    double destX = civilians.get(i).getX() + nc[i][X];
                    double destY = civilians.get(i).getY() + nc[i][Y];

                    double dx = destX - civilians.get(i).getX();
                    double dy = destY - civilians.get(i).getY();

                    double theta = Math.atan2(dy, dx);
                    double noise = (Math.random() - 0.5) * noiseAmp;
                    double mag = Math.sqrt(dx*dx + dy*dy);

                    // Nueva dirección con ruido
                    double finalDx = mag * Math.cos(theta + noise);
                    double finalDy = mag * Math.sin(theta + noise);

                    MoveResult res = civilians.get(i).move(DT, new double[]{
                            civilians.get(i).getX() + finalDx,
                            civilians.get(i).getY() + finalDy
                    });
                    if (res.equals(MoveResult.TRANSFORMED)) {
                        toRemoveCiv.add(civilians.get(i));
                    }
                }
                for (int i = 0; i < doctors.size(); i++) {
                    double destX = doctors.get(i).getX() + nd[i][X];
                    double destY = doctors.get(i).getY() + nd[i][Y];
                    double dx = destX - doctors.get(i).getX();
                    double dy = destY - doctors.get(i).getY();
                    double theta = Math.atan2(dy, dx);
                    double noise = (Math.random() - 0.5) * noiseAmp;
                    double mag = Math.sqrt(dx*dx + dy*dy);

                    // Nueva dirección con ruido
                    double finalDx = mag * Math.cos(theta + noise);
                    double finalDy = mag * Math.sin(theta + noise);
                    MoveResult res = doctors.get(i).move(DT, new double[]{
                            doctors.get(i).getX() + finalDx,
                            doctors.get(i).getY() + finalDy});
                    if (res.equals(MoveResult.TRANSFORMED)) {
                        toRemoveDoc.add(doctors.get(i));
                    }
                }
                for (int i = 0; i < zombies.size(); i++) {
                    double destX = zombies.get(i).getX() + nz[i][X];
                    double destY = zombies.get(i).getY() + nz[i][Y];
                    double dx = destX - zombies.get(i).getX();
                    double dy = destY - zombies.get(i).getY();
                    double theta = Math.atan2(dy, dx);
                    double noise = (Math.random() - 0.5) * noiseAmp;
                    double mag = Math.sqrt(dx*dx + dy*dy);

                    // Nueva dirección con ruido
                    double finalDx = mag * Math.cos(theta + noise);
                    double finalDy = mag * Math.sin(theta + noise);
                    MoveResult res = zombies.get(i).move(DT, new double[]{
                            zombies.get(i).getX() + finalDx,
                            zombies.get(i).getY() + finalDy});
                    if (res.equals(MoveResult.CURED)) {
                        toRemoveZom.add(zombies.get(i));
                    }
                }

                for (Civilian c : toRemoveCiv) {
                    civilians.remove(c);
                    zombies.add(new Zombie(c.getX(), c.getY(), c.getVx(), c.getVy(), false));
                }

                for (Doctor d : toRemoveDoc) {
                    doctors.remove(d);
                    zombies.add(new Zombie(d.getX(), d.getY(), d.getVx(), d.getVy(), true));
                }

                for (Zombie z : toRemoveZom) {
                    zombies.remove(z);
                    if (z.isDoctor())
                        doctors.add(new Doctor(z.getX(), z.getY(), z.getVx(), z.getVy()));
                    else
                        civilians.add(new Civilian(z.getX(), z.getY(), z.getVx(), z.getVy()));
                }

                for(Agent a : civilians) {
                    checkBounds(a);
                }
                for(Agent a : zombies) {
                    checkBounds(a);
                }
                for(Agent a : doctors) {
                    checkBounds(a);
                }

                currentTime += DT;
                return new Time(currentTime, civilians, zombies, doctors);
            }

            private void checkBounds(Agent a){
                double x = a.getX();
                double y = a.getY();
                double ar = a.getR();
                double r = borderRadius;
                double r2 = r * r;
                double d2 = x*x + y*y + ar;

                if (d2 > r2) {
                    double dist = Math.sqrt(d2);
                    double target = r - ar;
                    double scale = target / dist;
                    a.setX(x * scale);
                    a.setY(y * scale);
                }
            }


            private double[] getInteraction(Agent targetAgent, Agent sourceAgent, int a, int b) {
                double dx = targetAgent.getX() - sourceAgent.getX(); //Esto por ahi se pouede poner como metodo en Agent
                double dy = targetAgent.getY() - sourceAgent.getY();
                double modulus = Math.sqrt(dx * dx + dy * dy);
                double ex = dx / modulus;
                double ey = dy / modulus;

                double nx = ex * a * Math.exp(-modulus / b);
                double ny = ey * a * Math.exp(-modulus / b);

                boolean isThereCollision = modulus - targetAgent.getR() - sourceAgent.getR() < 0;
                if (isThereCollision && sourceAgent.getCollidingAgent().isEmpty() && targetAgent.getCollidingAgent().isEmpty()) {
                    sourceAgent.setCollidingAgent(targetAgent);
                    targetAgent.setCollidingAgent(sourceAgent);
                }
                return new double[]{nx, ny, modulus, ex, ey};
            }
        };

    }
}
