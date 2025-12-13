package ar.edu.itba.sds.engine;

public class Zombie extends Agent {

    private double[] targetDirection;
    private double distanceToTarget;

    public Zombie(double x, double y, double vx, double vy, boolean isDoctor) {
        super(x, y, vx, vy, isDoctor);
    }

    public Zombie infect(Agent a) {
        return new Zombie(a.getX(), a.getY(), a.getVx(), a.getVy(), a.isDoctor());
    }

    public void setTargetDirection(double targetDirection[]) {
        this.targetDirection = targetDirection;
    }

    public double[] getTargetDirection(){
        return targetDirection;
    }

    public double getDistanceToTarget() {
        return distanceToTarget;
    }

    public void setDistanceToTarget(double distanceToTarget) {
        this.distanceToTarget = distanceToTarget;
    }
}
