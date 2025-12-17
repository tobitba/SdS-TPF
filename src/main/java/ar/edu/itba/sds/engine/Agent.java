package ar.edu.itba.sds.engine;

import java.util.Optional;

public abstract class Agent {
    private double x;
    private double y;

    private double vx;
    private double vy;
    private static final double VMAX = 4;

    private double r;
    private static final double RMAX = 0.35;
    private static final double RMIN = 0.15;

    private final boolean isDoctor;
    private boolean isFighting = false;
    protected boolean isZombie;
    protected boolean lost = false;

    private final static double TAU = 0.5;
    private final static double BETA = 0.9;

    private final static int X = 0;
    private final static int Y = 1;

    private Agent collidingAgent = null;
    protected double timeFighting = 0;
    protected static final double fightingTime = 3;

    public Agent(double x, double y, double vx, double vy, boolean isDoctor) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.isDoctor = isDoctor;
        r = RMIN;
    }

    public MoveResult move(double dt, double[] targetPosition) {
        if (getCollidingAgent().isPresent()) {
            Agent collidingAgent = getCollidingAgent().get();
            if (isZombie() ^ collidingAgent.isZombie()) { // ^ is the XOR operator
                fightingOn();
                return handleZombieEvent(dt);
            }
            handleNormalRepulsion(dt);
            return MoveResult.NOTHING;
        }

        double dx = targetPosition[X] - x;
        double dy = targetPosition[Y] - y;
        double modulus = Math.sqrt(dx * dx + dy * dy);

        double unitDirVecX = dx / modulus;
        double unitDirVecY = dy / modulus;

        double vAbs;
        r = Math.min(RMAX, r + RMAX / (TAU / dt));
        vAbs = VMAX * Math.pow((r - RMIN) / (RMAX - RMIN), BETA);
        vx = unitDirVecX * vAbs;
        vy = unitDirVecY * vAbs;

        x = x + vx * dt;
        y = y + vy * dt;

        return MoveResult.NOTHING;
    }

    private void handleNormalRepulsion(double dt) {
        Agent collidingAgent = getCollidingAgent().orElseThrow(IllegalStateException::new);

        double dx = x - collidingAgent.getX();
        double dy = y - collidingAgent.getY();
        double modulus = Math.sqrt(dx * dx + dy * dy);

        double unitDirVecX = dx / modulus;
        double unitDirVecY = dy / modulus;

        double vAbs;
        r = RMIN;
        vAbs = VMAX;
        vx = unitDirVecX * vAbs;
        vy = unitDirVecY * vAbs;

        x = x + vx * dt;
        y = y + vy * dt;
    }

    protected abstract MoveResult handleZombieEvent(double dt);

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getR() {
        return r;
    }

    public boolean isDoctor() {
        return isDoctor;
    }

    @Override
    public String toString(){
        return x + "," + y + "," + r + "," + isFighting;
    }

    public static double getRmin(){
        return RMIN;
    }

    public Optional<Agent> getCollidingAgent() {
        return Optional.ofNullable(collidingAgent);
    }

    public void setCollidingAgent(Agent a) {
        this.collidingAgent = a;
    }

    public boolean isZombie() {
        return isZombie;
    }

    public boolean isFighting() {
        return isFighting;
    }

    public void fightingOn() {
        isFighting = true;
    }

    public void fightingOff() {
        isFighting = false;
        timeFighting = 0;
        collidingAgent = null;
    }

    protected boolean lost() {
        return lost;
    }
}
