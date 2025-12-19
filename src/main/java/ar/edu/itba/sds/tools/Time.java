package ar.edu.itba.sds.tools;

import ar.edu.itba.sds.engine.*;

import java.util.List;

public record Time (double time, List<Civilian> civilians, List<Zombie> zombies, List<Doctor> doctors){
    public double getMeanV() {
        double v=0;
        for(Civilian civ : civilians) {
            v+= Math.sqrt(civ.getVy()*civ.getVy()+civ.getVx()*civ.getVx());
        }
        for(Zombie zombie : zombies) {
            v+= Math.sqrt(zombie.getVx()*zombie.getVx()+zombie.getVy()*zombie.getVy());
        }
        for(Doctor doctor : doctors) {
            v+= Math.sqrt(doctor.getVx()*doctor.getVx()+doctor.getVy()*doctor.getVy());
        }
        int size = zombies.size()+doctors.size()+civilians.size();
        v /= size;
        return v;
    }
}
