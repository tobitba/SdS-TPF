package ar.edu.itba.sds.engine;

public class Agent {
    private double x;
    private double y;

    private double vx;
    private double vy;
    private static final double VMAX = 4;

    private double r;
    private static final double RMAX = 0.15;
    private static final double RMIN = 0.35;

    private final boolean isDoctor;

    private final static double TAU = 0.5;
    private final static double BETA = 0.9;

    public Agent(double x, double y, double vx, double vy, boolean isDoctor) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.isDoctor = isDoctor;
        r = RMIN;
    }

    public void move(double dt, double[] unitDirVec, boolean inContact) {
        double vabs;
        if (inContact) {
            r = RMIN;
            vabs = VMAX;
        }
        else {
            r = Math.min(RMAX, r + RMAX / (TAU / dt));
            vabs = VMAX * Math.pow((r - RMIN) / (RMAX - RMIN), BETA);
        }
        vx = unitDirVec[0] * vabs;
        vy = unitDirVec[1] * vabs;

        x = x + vx * dt;
        y = y + vy * dt;
    }

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

    public boolean isDoctor() {
        return isDoctor;
    }

    @Override
    public String toString(){
        return x + " " + y;
    }

    public static double getRmin(){
        return RMIN;
    }
}
