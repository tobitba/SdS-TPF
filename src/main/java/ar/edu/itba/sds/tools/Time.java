package ar.edu.itba.sds.tools;

import ar.edu.itba.sds.engine.*;

import java.util.List;

public record Time (double time, List<Civilian> civilians, List<Zombie> zombies, List<Doctor> doctors){}
