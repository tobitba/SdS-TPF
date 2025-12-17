package ar.edu.itba.sds;

import ar.edu.itba.sds.engine.*;
import ar.edu.itba.sds.tools.PostProcessor;
import ar.edu.itba.sds.tools.SimulationViewer;
import ar.edu.itba.sds.tools.Time;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class App {
    public static void main(String[] args)  {

        Field field = new Field(11,1,0, 10);
        Iterator<Time> timeIterator = field.iterator();
        try(PostProcessor postProcessor = new PostProcessor("dynamicOutput.txt")) {
            timeIterator.forEachRemaining(postProcessor::processEpoch);
        }catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
