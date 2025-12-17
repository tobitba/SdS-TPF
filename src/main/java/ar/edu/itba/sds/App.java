package ar.edu.itba.sds;

import ar.edu.itba.sds.engine.*;
import ar.edu.itba.sds.tools.SimulationViewer;
import ar.edu.itba.sds.tools.Time;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class App {
    public static void main(String[] args) {
       /* List<Civilian> civilians = new ArrayList<>();
        List<Doctor> doctors = new ArrayList<>();
        List<Zombie> zombies = new ArrayList<>();
        HumanGenerator.generateAgents(100,10,civilians::add,doctors::add, zombies::add, 11);
        List<Agent> agents = new ArrayList<>();
        agents.addAll(civilians);
        agents.addAll(doctors);
        agents.addAll(zombies);*/
        //SimulationViewer.show(agents,11);

        Field field = new Field(11,11,0, 10);
        Iterator<Time> timeIterator = field.iterator();
        timeIterator.forEachRemaining(t -> {
            List<Agent> agents = new ArrayList<>();
            agents.addAll(t.civilians());
            agents.addAll(t.zombies());
            agents.addAll(t.doctors());

            SimulationViewer.show(agents, 11);
        });
    }
}
