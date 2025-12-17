package ar.edu.itba.sds.engine;

import java.util.Random;

public class Doctor extends Agent {
    private static final Random fightingRandomizer = new Random();

    public Doctor(double x, double y, double vx, double vy) {
        super(x, y, vx, vy, true);
        isZombie = false;
    }

    // Importante manejar esto primero antes que el zombie
    @Override
    protected MoveResult handleZombieEvent(double dt) {
        if (timeFighting >= fightingTime) {
            fightingOff();
            double fightingResult = fightingRandomizer.nextDouble();
            if (fightingResult < 0.6)
                return MoveResult.NOTHING;
            lost = true;
            return MoveResult.TRANSFORMED;
        }

        timeFighting += dt;
        return MoveResult.FIGHTING;
    }
}
