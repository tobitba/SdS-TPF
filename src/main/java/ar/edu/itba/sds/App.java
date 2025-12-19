package ar.edu.itba.sds;

import ar.edu.itba.sds.engine.*;
import ar.edu.itba.sds.tools.PostProcessor;
import ar.edu.itba.sds.tools.Time;

import java.io.IOException;
import java.util.Iterator;

public class App {
    private static final String CIVILIAN_COUNT = "hc";
    private static final String DOCTOR_COUNT = "hd";
    private static final String MAX_TIME = "duration";
    private static final String OUTPUT = "output";

    public static void main(String[] args)  {
        int civilianCount = Integer.parseInt(System.getProperty(CIVILIAN_COUNT));
        int doctorCount = Integer.parseInt(System.getProperty(DOCTOR_COUNT));
        double maxTime = Double.parseDouble(System.getProperty(MAX_TIME));
        String output = System.getProperty(OUTPUT);

        Field field = new Field(11, civilianCount, doctorCount, maxTime);
        Iterator<Time> timeIterator = field.iterator();
        try(PostProcessor postProcessor = new PostProcessor(output)) {
            postProcessor.processEpoch(field.getCurrentTime()); //t = 0
            timeIterator.forEachRemaining(postProcessor::processEpoch);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
