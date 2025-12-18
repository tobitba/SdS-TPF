package ar.edu.itba.sds.engine;

public class Zombie extends Agent {

    private double[] targetDirection;
    private double distanceToTarget = Double.MAX_VALUE;

    public Zombie(double x, double y, double vx, double vy, boolean isDoctor) {
        super(x, y, vx, vy, isDoctor);
        isZombie = true;
    }

    @Override
    public MoveResult move(double dt, double[] targetPosition){
        MoveResult moveResult = super.move(dt,targetPosition);
        setDistanceToTarget(Double.MAX_VALUE);
        return moveResult;
    }
    public void setTargetDirection(double[] targetDirection) {
        this.targetDirection = targetDirection;
    }

    public double[] getTargetDirection(){
        if(targetDirection == null){
            return new double[]{0, 0};
        }
        return targetDirection;
    }

    public double getDistanceToTarget() {
        return distanceToTarget;
    }

    public void setDistanceToTarget(double distanceToTarget) {
        this.distanceToTarget = distanceToTarget;
    }

    @Override
    protected MoveResult handleZombieEvent(double dt) {
        if (timeFighting >= fightingTime) {
            Agent collidingAgent = getCollidingAgent().orElseThrow();
            fightingOff();
            if (collidingAgent.lost())
                return MoveResult.NOTHING;
            else
                return MoveResult.CURED;
        }

        timeFighting += dt;
        return MoveResult.FIGHTING;
    }
}
