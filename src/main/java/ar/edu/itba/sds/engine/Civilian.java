package ar.edu.itba.sds.engine;

public class Civilian extends Agent {
    public Civilian(double x, double y, double vx, double vy) {
        super(x, y, vx, vy, false);
        isZombie = false;
    }

    @Override
    protected MoveResult handleZombieEvent(double dt) {
        if (timeFighting >= fightingTime) {
            fightingOff();
            lost = true;
            return MoveResult.TRANSFORMED;
        }

        timeFighting += dt;
        return MoveResult.FIGHTING;
    }
}
