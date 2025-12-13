package ar.edu.itba.sds.engine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Field implements Iterable<Time>{
    private List<Zombie> zombies;
    private List<Civilian> civilians;
    private List<Doctor> doctors;
    private List<Agent> allAgents;
    private double borderRadius;

    public Field(double borderRadius) {
        this.zombies = new ArrayList<>();
        this.civilians = new ArrayList<>();
        this.doctors = new ArrayList<>();
        this.borderRadius = borderRadius;
    }

    public void addZombie(Zombie zombie) {
        zombies.add(zombie);
        allAgents.add(zombie);
    }

    public void addCivilian(Civilian civilian) {
        civilians.add(civilian);
        allAgents.add(civilian);
    }

    public void addDoctor(Doctor doctor) {
        doctors.add(doctor);
        allAgents.add(doctor);
    }

    @Override
    public Iterator<Time> iterator() {
        return null;
    }
}
