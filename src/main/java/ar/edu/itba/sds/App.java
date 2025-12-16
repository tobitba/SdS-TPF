package ar.edu.itba.sds;

import ar.edu.itba.sds.engine.Agent;
import ar.edu.itba.sds.engine.Civilian;
import ar.edu.itba.sds.engine.Doctor;
import ar.edu.itba.sds.engine.Zombie;
import ar.edu.itba.sds.tools.HumanGenerator;
import ar.edu.itba.sds.tools.SimulationViewer;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        List<Civilian> civilians = new ArrayList<>();
        List<Doctor> doctors = new ArrayList<>();
        List<Zombie> zombies = new ArrayList<>();
        HumanGenerator.generateAgents(1000,3,civilians::add,doctors::add, zombies::add, 11);
        List<Agent> agents = new ArrayList<>();
        agents.addAll(civilians);
        agents.addAll(zombies);
        SimulationViewer.show(agents,11);
    }
}
