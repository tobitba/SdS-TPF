package ar.edu.itba.sds.engine;

public class Zombie extends Agent {

    public Zombie(double x, double y, double vx, double vy, boolean isDoctor) {
        super(x, y, vx, vy, isDoctor);
    }

    public Zombie infect(Agent a) {
        return new Zombie(a.getX(), a.getY(), a.getVx(), a.getVy(), a.isDoctor());
    }
}
