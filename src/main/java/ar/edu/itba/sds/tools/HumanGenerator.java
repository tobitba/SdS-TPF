package ar.edu.itba.sds.tools;

import ar.edu.itba.sds.engine.Civilian;
import ar.edu.itba.sds.engine.Doctor;
import ar.edu.itba.sds.engine.Zombie;

import java.util.function.Consumer;

public class HumanGenerator {
    public static void generate(int humanCount, int zombieCount, int doctorCount,
                                 Consumer<Civilian> civilians, Consumer<Zombie> zombies, Consumer<Doctor> doctors,
                                 double fieldRadius) {
        //TODO: hacer el metodo :)

    }

    private static void generateCivilians(int civilianCount, Consumer<Civilian> civilians, double fieldRadius){
        //...
    }
}
